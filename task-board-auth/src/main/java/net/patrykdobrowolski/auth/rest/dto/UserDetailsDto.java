package net.patrykdobrowolski.auth.rest.dto;

import lombok.Builder;
import lombok.Getter;
import net.patrykdobrowolski.auth.domain.AuthProvider;

@Getter
@Builder
public class UserDetailsDto {

    private String username;
    private AuthProvider authProvider;
    private String providerId;
    private String displayName;
}
