package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

@Builder
public record AuthenticationResult(User user, String accessToken, String refreshToken) {}
