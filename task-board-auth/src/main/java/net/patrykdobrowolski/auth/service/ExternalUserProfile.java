package net.patrykdobrowolski.auth.service;

public record ExternalUserProfile(
    String userId,
    String username,
    String email,
    String name)
{}
