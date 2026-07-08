package net.patrykdobrowolski.auth.db.repository;

import net.patrykdobrowolski.auth.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByLogin(String login);
}
