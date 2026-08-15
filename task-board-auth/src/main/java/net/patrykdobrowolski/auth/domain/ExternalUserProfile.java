package net.patrykdobrowolski.auth.domain;

import lombok.Builder;

@Builder
public record ExternalUserProfile(
    String userId,
    String username,
    String name)
{}
