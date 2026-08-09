package net.patrykdobrowolski.auth.service;

public interface OAuth2AuthenticationProvider {

    boolean supports(AuthProvider authProvider);
    ExternalUserProfile authenticate(String tokenString) throws Exception;
}
