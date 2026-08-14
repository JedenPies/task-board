package net.patrykdobrowolski.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import net.patrykdobrowolski.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenGenerator {

    private final SecretKey key;
    private final long expirationMs;
    private final Clock clock;

    public TokenGenerator(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            Clock clock
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.clock = clock;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now(clock);
        Instant validity = now.plus(expirationMs, ChronoUnit.MILLIS);
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
//                .claim("roles", "USER")
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return UUID.randomUUID().toString();
    }
}
