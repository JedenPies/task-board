package net.patrykdobrowolski.auth.db.repository;

import net.patrykdobrowolski.auth.db.entity.UserEntity;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUsersRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);
}
