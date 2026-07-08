package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AuthenticationResultDto(String accessToken) {

}
