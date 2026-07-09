package net.patrykdobrowolski.auth.service;

import net.patrykdobrowolski.auth.db.repository.UserTokensRepositoryService;
import net.patrykdobrowolski.auth.db.repository.UsersRepositoryService;
import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.TokensPair;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.UserToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.patrykdobrowolski.auth.domain.InvalidRefreshTokenException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UsersRepositoryService usersRepository;
    @Mock
    private UserTokensRepositoryService userTokensRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenGenerator tokenGenerator;
    @Mock
    private Clock clock;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String DUMMY_HASH_VALUE = "dummy_hash_value";

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn(DUMMY_HASH_VALUE);
        authenticationService.prepareDummyHash();
    }

    @Test
    void shouldThrowInvalidCredentialsAndPreventTimingAttackWhenUserNotFound() {

        // given
        String login = "unknown_user";
        AuthenticateWithPasswordCommand command = new AuthenticateWithPasswordCommand(login, "password123");

        when(usersRepository.findByLogin(login)).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), eq(DUMMY_HASH_VALUE))).thenReturn(false);

        // when
        assertThatThrownBy(() -> authenticationService.authenticate(command)).isInstanceOf(InvalidCredentialsException.class);

        // then
        verify(passwordEncoder).matches(anyString(), eq(DUMMY_HASH_VALUE));
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordIsWrong() {
        // given
        String login = "existing_user";
        String wrongPassword = "wrongPassword";
        String encodedRealPassword = "encodedRealPassword";
        AuthenticateWithPasswordCommand command = new AuthenticateWithPasswordCommand(login, wrongPassword);

        User user = User.builder().login(login).passwordEncoded(encodedRealPassword).build();

        when(usersRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, encodedRealPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authenticationService.authenticate(command)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldAuthenticateAndReturnTokensWhenCredentialsAreValid() throws InvalidCredentialsException {
        // given
        String login = "existing_user";
        String correctPassword = "correctPassword";
        String encodedRealPassword = "encodedRealPassword";
        AuthenticateWithPasswordCommand command = new AuthenticateWithPasswordCommand(login, correctPassword);

        User user = User.builder().login(login).passwordEncoded(encodedRealPassword).build();

        when(usersRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(correctPassword, encodedRealPassword)).thenReturn(true);
        when(tokenGenerator.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(tokenGenerator.generateAccessToken(user)).thenReturn("new-access-token");
        when(clock.instant()).thenReturn(Instant.now());

        // when
        TokensPair result = authenticationService.authenticate(command);

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");

        verify(userTokensRepository).save(any(UserToken.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsNotFound() {
        // given
        String oldToken = "non-existent-token";
        when(userTokensRepository.findByRefreshToken(oldToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authenticationService.refresh(oldToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldGenerateNewTokensAndRevokeOldWhenRefreshingValidToken() throws InvalidRefreshTokenException {
        // given
        String oldToken = "valid-old-token";
        User user = User.builder().login("test_user").build();

        UserToken currentToken = mock(UserToken.class);

        when(userTokensRepository.findByRefreshToken(oldToken)).thenReturn(Optional.of(currentToken));
        when(currentToken.use(clock)).thenReturn(Optional.empty());
        when(currentToken.getUser()).thenReturn(user);

        when(tokenGenerator.generateRefreshToken(user)).thenReturn("brand-new-refresh-token");
        when(tokenGenerator.generateAccessToken(user)).thenReturn("brand-new-access-token");
        when(clock.instant()).thenReturn(Instant.now()); // Potrzebne do zapisania nowego tokena (Instant.now(clock))

        // when
        TokensPair result = authenticationService.refresh(oldToken);

        // then
        assertThat(result.accessToken()).isEqualTo("brand-new-access-token");
        assertThat(result.refreshToken()).isEqualTo("brand-new-refresh-token");

        verify(currentToken).replacedBy("brand-new-refresh-token");

        verify(userTokensRepository).save(currentToken);
        verify(userTokensRepository, times(2)).save(any(UserToken.class));
    }

    @Test
    void shouldReturnReplacementTokenDuringGracePeriod() throws InvalidRefreshTokenException {
        // given
        String oldToken = "revoked-but-in-grace-period-token";
        String replacementRefreshToken = "already-generated-replacement-token";
        User user = User.builder().login("test_user").build();

        UserToken currentToken = mock(UserToken.class);

        when(userTokensRepository.findByRefreshToken(oldToken)).thenReturn(Optional.of(currentToken));
        when(currentToken.use(clock)).thenReturn(Optional.of(replacementRefreshToken));
        when(currentToken.getUser()).thenReturn(user);

        when(tokenGenerator.generateAccessToken(user)).thenReturn("new-access-token-for-grace");

        // when
        TokensPair result = authenticationService.refresh(oldToken);

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token-for-grace");
        assertThat(result.refreshToken()).isEqualTo(replacementRefreshToken);

        verify(userTokensRepository).save(currentToken);
        verify(tokenGenerator, never()).generateRefreshToken(any());
    }
}