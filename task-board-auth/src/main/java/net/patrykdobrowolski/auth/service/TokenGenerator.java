package net.patrykdobrowolski.auth.service;

import net.patrykdobrowolski.auth.domain.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenGenerator {

    public String generateAccessToken(User user) {
        return "some-token";
    }
    public String generateRefreshToken(User user) {
        return "refresh:" + UUID.randomUUID();
    }
}
