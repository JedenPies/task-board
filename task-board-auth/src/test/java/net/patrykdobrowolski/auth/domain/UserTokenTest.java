package net.patrykdobrowolski.auth.domain;

import net.patrykdobrowolski.auth.domain.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTokenTest {

    // Ustawiamy "zamrożony" punkt w czasie dla naszych testów
    private final Instant NOW = Instant.parse("2024-01-01T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

    @Test
    void shouldSuccessfullyUseUnrevokedToken() throws InvalidRefreshTokenException {

        // given
        UserToken token = UserToken.builder()
                .validUntil(NOW.plus(30, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        // when
        Optional<String> result = token.use(clock);

        // then
        assertThat(result).isEmpty();
        assertThat(token.getIsRevoked()).isTrue();
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {

        // given
        UserToken token = UserToken.builder()
                .validUntil(NOW.minus(1, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();

        // when & then
        assertThatThrownBy(() -> token.use(clock))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldReturnReplacementTokenWhenUsedWithinGracePeriod() throws InvalidRefreshTokenException {

        // given
        String replacementToken = "new-refresh-token-123";
        UserToken token = UserToken.builder()
                .validUntil(NOW.plus(30, ChronoUnit.DAYS))
                .isRevoked(true)
                .usedAt(NOW) // Został zużyty w punkcie "NOW"
                .replacedBy(replacementToken)
                .build();

        Clock clockAfter5Seconds = Clock.offset(clock, Duration.ofSeconds(5));

        // when
        Optional<String> result = token.use(clockAfter5Seconds);

        // then
        assertThat(result).isPresent().contains(replacementToken);
    }

    @Test
    void shouldThrowExceptionWhenUsedAfterGracePeriod() {

        // given
        UserToken token = UserToken.builder()
                .validUntil(NOW.plus(30, ChronoUnit.DAYS))
                .isRevoked(true)
                .usedAt(NOW) // Został zużyty w punkcie "NOW"
                .replacedBy("new-refresh-token-123")
                .build();

        Clock clockAfter11Seconds = Clock.offset(clock, Duration.ofSeconds(11));

        // when & then
        assertThatThrownBy(() -> token.use(clockAfter11Seconds))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsRevokedButUsedAtIsNull() {

        // given
        UserToken token = UserToken.builder()
                .validUntil(NOW.plus(30, ChronoUnit.DAYS))
                .isRevoked(true)
                .usedAt(null)
                .build();

        // when & then
        assertThatThrownBy(() -> token.use(clock))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
