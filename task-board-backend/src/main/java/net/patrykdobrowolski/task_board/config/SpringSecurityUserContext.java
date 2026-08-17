package net.patrykdobrowolski.task_board.config;

import net.patrykdobrowolski.task_board.domain.UserContext;
import net.patrykdobrowolski.task_board.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityUserContext implements UserContext {

    @Override
    public UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        if (auth.getPrincipal() instanceof UserPrincipal(UUID userId)) {
            return userId;
        }
        return null;
    }
}
