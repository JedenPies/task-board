package net.patrykdobrowolski.task_board.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.patrykdobrowolski.task_board.domain.UserContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;
    private final UserContext userContext;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String secretKey,
            UserContext userContext,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.userContext = userContext;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                var claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();
                var auth = new JwtAuthenticationToken(claims.getSubject(), jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);
                userContext.setUserName(claims.getSubject());
            } catch (ExpiredJwtException e) {
                exceptionResolver.resolveException(request, response, null, e);
                return;
            } catch (JwtException e) {
                log.warn("Invalid or tampered JWT token", e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static class JwtAuthenticationToken extends AbstractAuthenticationToken {

        private final String principal;
        private final String jwt;

        public JwtAuthenticationToken(String principal, String jwt) {
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
}
