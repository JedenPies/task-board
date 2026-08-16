package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import net.patrykdobrowolski.auth.domain.AuthProvider;

import java.util.UUID;

@Jacksonized
@Builder
public record AuthenticationResultDto(String accessToken, UUID userId, String userDisplayName, AuthProvider authProvider) {

}
