package net.patrykdobrowolski.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.repository.UserTokensRepositoryService;
import net.patrykdobrowolski.auth.db.repository.UsersRepositoryService;
import net.patrykdobrowolski.auth.domain.AuthenticationRequest;
import net.patrykdobrowolski.auth.domain.TokensPair;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.UserToken;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepositoryService usersRepository;
    private final UserTokensRepositoryService userTokensRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    private String dummyHash;

    @PostConstruct
    public void prepareDummyHash() {
        dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public TokensPair authenticate(AuthenticationRequest request) throws InvalidCredentialsException {
        User userFound = usersRepository.findByLogin(request.login()).orElseThrow(this::invalidCredentialsExceptionAfterEmptyHashing);
        checkPassword(userFound, request.password());
        TokensPair tokensPair = generateTokens(userFound);
        registerToken(userFound, tokensPair);
        return tokensPair;
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

    private TokensPair generateTokens(User userFound) {
        return TokensPair.builder()
                .accessToken(tokenGenerator.generateAccessToken(userFound))
                .refreshToken(tokenGenerator.generateRefreshToken(userFound))
                .build();
    }

    private void registerToken(User userFound, TokensPair result) {
        UserToken newToken = UserToken.builder()
                .user(userFound)
                .refreshToken(result.refreshToken())
                .validUntil(Instant.now(clock).plus(30, ChronoUnit.DAYS)).build();
        userTokensRepository.save(newToken);
    }
}
