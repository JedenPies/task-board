package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import net.patrykdobrowolski.auth.service.AuthProvider;

@Jacksonized
@Builder
public record AuthenticationResultDto(String accessToken, String userDisplayName, AuthProvider authProvider) {

}
