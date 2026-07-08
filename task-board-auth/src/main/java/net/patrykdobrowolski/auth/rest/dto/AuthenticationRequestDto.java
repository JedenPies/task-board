package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AuthenticationRequestDto(String login, String password) {

}
