package net.patrykdobrowolski.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.patrykdobrowolski.auth.domain.exception.InvalidRefreshTokenException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Builder @Getter
@AllArgsConstructor @NoArgsConstructor
public class UserToken {

    private UUID id;
    private User user;
    private String refreshToken;
    private Instant validUntil;
    private Instant usedAt;
    @Builder.Default
    private Boolean isRevoked = false;
    private String replacedBy;

    public Optional<String> use(Clock clock) throws InvalidRefreshTokenException {
        checkValidity(clock);
        if (Boolean.TRUE.equals(isRevoked)) {
            if (usedAt == null || isAfterGracePeriod(clock)) {
                throw new InvalidRefreshTokenException();
            }
            return Optional.ofNullable(replacedBy);
        }
        this.usedAt = Instant.now(clock);
        this.isRevoked = true;
        return Optional.empty();
    }

    public void replacedBy(String newToken) {
        this.replacedBy = newToken;
    }

    public void revoke() {
        this.isRevoked = true;
    }

    private boolean isAfterGracePeriod(Clock clock) {
        return Instant.now(clock).isAfter(usedAt.plus(10, ChronoUnit.SECONDS));
    }

    private void checkValidity(Clock clock) throws InvalidRefreshTokenException {
        if (validUntil.isBefore(Instant.now(clock))) {
            throw new InvalidRefreshTokenException();
        }
    }
}
