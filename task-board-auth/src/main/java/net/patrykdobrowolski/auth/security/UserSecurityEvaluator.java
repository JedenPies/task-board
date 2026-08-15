package net.patrykdobrowolski.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component("usersSecurity")
public class UserSecurityEvaluator {

    public boolean canViewUser(Authentication authentication, UUID userId) {
        return isAuthenticated(authentication) && isOwner(authentication, userId);
    }

    public boolean canDeleteUser(Authentication authentication, UUID userId) {
        return isAuthenticated(authentication) && isOwner(authentication, userId);
    }

    public boolean canUpdateUser(Authentication authentication, UUID userId) {
        return isAuthenticated(authentication) && isOwner(authentication, userId);
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null;
    }

    private static boolean isOwner(Authentication authentication, UUID userId) {
        if ((authentication.getPrincipal() instanceof UserPrincipal(UUID id))) {
            return Objects.equals(id, userId);
        }
        return false;
    }
}
