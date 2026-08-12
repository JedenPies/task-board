package net.patrykdobrowolski.auth.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.repository.UserTokensRepositoryService;
import net.patrykdobrowolski.auth.db.repository.UsersRepositoryService;
import net.patrykdobrowolski.auth.domain.*;
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
public class AuthenticationService {

    private final UsersRepositoryService usersRepository;
    private final UserTokensRepositoryService userTokensRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;
    private final OAuth2AuthenticationProviderFactory oAuth2AuthenticationProviderFactory;
    private final UsersService usersService;

    private String dummyHash;

    @PostConstruct
    public void prepareDummyHash() {
        dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public AuthenticationResult authenticate(AuthenticateWithPasswordCommand request) throws InvalidCredentialsException {
        User userFound = usersRepository.findByUsername(request.username()).orElseThrow(this::invalidCredentialsExceptionAfterEmptyHashing);
        checkPassword(userFound, request.password());
        return generateAndRegisterTokens(userFound);
    }

    public AuthenticationResult authenticate(AuthenticateWithExternalProviderCommand command) throws Exception {
        ExternalUserProfile userProfile = oAuth2AuthenticationProviderFactory.getProvider(command.getProvider()).authenticate(command.getToken());
        ExternalUserLoginData loginData = ExternalUserLoginData.of(command.getProvider(), userProfile);
        User user = usersRepository.findByExternalUserLoginData(loginData).orElseGet(
                () -> usersService.createNewUser(command.getProvider(), userProfile));
        return generateAndRegisterTokens(user);
    }

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
