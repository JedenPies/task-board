package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder @Getter
public class OAuth2LoginRequestDto {

    private final String token;
}
