package net.patrykdobrowolski.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationProviderFactory {

    private final List<OAuth2AuthenticationProvider> providers;

    public OAuth2AuthenticationProvider getProvider(AuthProvider authProvider) {
        return providers.stream()
                .filter(provider -> provider.supports(authProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No provider found for auth provider: " + authProvider));
    }
}
