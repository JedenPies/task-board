package net.patrykdobrowolski.auth.service;

import lombok.Builder;

@Builder
public record ExternalUserProfile(
    String userId,
    String username,
    String email,
    String name)
{}
