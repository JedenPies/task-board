package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder @Getter
public class UpdateUserDetailsDto {

    private String displayName;
}
