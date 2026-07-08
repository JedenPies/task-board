package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized @Getter
public class NewUserDto {

    private final String login;
    private final String password;
}
