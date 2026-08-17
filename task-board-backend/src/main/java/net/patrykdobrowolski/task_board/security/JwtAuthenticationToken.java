package net.patrykdobrowolski.task_board.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class JwtAuthenticationToken  extends AbstractAuthenticationToken {

    private final UserPrincipal principal;
    private final String jwt;

    public JwtAuthenticationToken(UserPrincipal principal, String jwt) {
        super(Collections.emptyList());
        this.principal = principal;
        this.jwt = jwt;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return principal;
    }

    @Override
    public @Nullable Object getCredentials() {
        return jwt;
    }
}
