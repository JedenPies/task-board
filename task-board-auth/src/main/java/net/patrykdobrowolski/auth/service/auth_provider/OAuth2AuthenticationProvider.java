package net.patrykdobrowolski.auth.service.auth_provider;

import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.ExternalUserProfile;

public interface OAuth2AuthenticationProvider {

    boolean supports(AuthProvider authProvider);
    ExternalUserProfile authenticate(String tokenString) throws Exception;
}
