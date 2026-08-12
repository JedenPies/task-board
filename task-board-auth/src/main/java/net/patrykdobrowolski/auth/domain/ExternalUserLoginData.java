package net.patrykdobrowolski.auth.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.service.AuthProvider;
import net.patrykdobrowolski.auth.service.ExternalUserProfile;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalUserLoginData {

    private final AuthProvider provider;
    private final String username;

    public String asUsername() {
        return provider.name() + ":" + username;
    }

    public static ExternalUserLoginData of(AuthProvider authProvider, ExternalUserProfile profile) {
        return new ExternalUserLoginData(authProvider, profile.username());
    }
}
