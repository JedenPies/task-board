package net.patrykdobrowolski.auth.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthenticateWithExternalProviderCommand {

    private final AuthProvider provider;
    private final String token;
}
