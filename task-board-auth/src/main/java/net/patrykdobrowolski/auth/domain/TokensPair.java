package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

@Builder
public record TokensPair(String accessToken, String refreshToken) {}
