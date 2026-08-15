package net.patrykdobrowolski.auth.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.exception.InvalidCredentialsException;
import net.patrykdobrowolski.auth.domain.port.in.AuthenticationUseCase;
import net.patrykdobrowolski.auth.domain.port.out.UserTokensRepository;
import net.patrykdobrowolski.auth.domain.port.in.UsersUseCase;
import net.patrykdobrowolski.auth.domain.port.out.UsersRepository;
import net.patrykdobrowolski.auth.domain.*;
import net.patrykdobrowolski.auth.domain.exception.InvalidRefreshTokenException;
import net.patrykdobrowolski.auth.service.auth_provider.OAuth2AuthenticationProviderFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {

    private final UsersRepository usersRepository;
    private final UserTokensRepository userTokensRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;
    private final OAuth2AuthenticationProviderFactory oAuth2AuthenticationProviderFactory;
    private final UsersUseCase usersUseCase;

    private String dummyHash;

    @PostConstruct
    public void prepareDummyHash() {
        dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public AuthenticationResult authenticate(AuthenticateWithPasswordCommand request) throws InvalidCredentialsException {
        User userFound = usersRepository.findByUsername(request.username()).orElseThrow(this::invalidCredentialsExceptionAfterEmptyHashing);
        checkPassword(userFound, request.password());
        return generateAndRegisterTokens(userFound);
    }

    @Override
    public AuthenticationResult authenticate(AuthenticateWithExternalProviderCommand command) throws Exception {
        ExternalUserProfile userProfile = oAuth2AuthenticationProviderFactory.getProvider(command.getProvider()).authenticate(command.getToken());
        User user = usersRepository.findByExternalAuthProvider(command.getProvider(), userProfile.userId())
                .orElseGet(() -> usersUseCase.createNewUser(command.getProvider(), userProfile));
        return generateAndRegisterTokens(user);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String oldToken) throws InvalidRefreshTokenException {
        UserToken current = userTokensRepository.findByRefreshToken(oldToken).orElseThrow(InvalidRefreshTokenException::new);
        Optional<String> replacement = current.use(clock);
        AuthenticationResult newAuthenticationResult = replacement.isEmpty()
            ? replaceWithNew(current)
            : generateAccessToken(current.getUser(), replacement.get());
        userTokensRepository.save(current);
        return newAuthenticationResult;
    }

    @Override
    @Transactional
    public void logout(String oldToken) throws InvalidRefreshTokenException {
        UserToken current = userTokensRepository.findByRefreshToken(oldToken).orElseThrow(InvalidRefreshTokenException::new);
        current.revoke();
        userTokensRepository.save(current);
    }

    private @NonNull AuthenticationResult replaceWithNew(UserToken current) {
        AuthenticationResult authenticationResult = generateAndRegisterTokens(current.getUser());
        current.replacedBy(authenticationResult.refreshToken());
        return authenticationResult;
    }

    private @NonNull InvalidCredentialsException invalidCredentialsExceptionAfterEmptyHashing() {
        passwordEncoder.matches(UUID.randomUUID().toString(), dummyHash);
        return new InvalidCredentialsException();
    }

    private void checkPassword(User user, String password) throws InvalidCredentialsException {
        if (!passwordEncoder.matches(password, user.getPasswordEncoded())) {
            throw new InvalidCredentialsException();
        }
    }

    private AuthenticationResult generateAndRegisterTokens(User userFound) {
        String refreshToken = tokenGenerator.generateRefreshToken(userFound);
        AuthenticationResult authenticationResult = generateAccessToken(userFound, refreshToken);
        registerToken(userFound, authenticationResult);
        return authenticationResult;
    }

    private AuthenticationResult generateAccessToken(User userFound, String refreshToken) {
        return AuthenticationResult.builder()
                .user(userFound)
                .accessToken(tokenGenerator.generateAccessToken(userFound))
                .refreshToken(refreshToken)
                .build();
    }

    private void registerToken(User userFound, AuthenticationResult result) {
        UserToken newToken = UserToken.builder()
                .user(userFound)
                .refreshToken(result.refreshToken())
                .validUntil(Instant.now(clock).plus(30, ChronoUnit.DAYS))
                .build();
        userTokensRepository.save(newToken);
    }
}
