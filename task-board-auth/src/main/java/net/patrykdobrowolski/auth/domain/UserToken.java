package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserToken(UUID id, User user, String refreshToken, Instant validUntil) {

}
