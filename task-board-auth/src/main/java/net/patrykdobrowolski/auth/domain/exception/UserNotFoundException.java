package net.patrykdobrowolski.auth.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class UserNotFoundException extends Exception {

    private final String username;
    private final UUID userId;

    public static UserNotFoundException of(String username) {
        return new UserNotFoundException(username, null);
    }

    public static UserNotFoundException of(UUID userId) {
        return new UserNotFoundException(null, userId);
    }
}
