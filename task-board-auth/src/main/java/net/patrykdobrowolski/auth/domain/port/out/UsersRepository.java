package net.patrykdobrowolski.auth.domain.port.out;

import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository {

    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(UUID userId);
    Optional<User> findByExternalAuthProvider(AuthProvider authProvider, String providerId);
    User save(User user);
}
