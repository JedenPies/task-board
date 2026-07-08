package net.patrykdobrowolski.auth.db.repository;

import net.patrykdobrowolski.auth.db.entity.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTokensRepository extends JpaRepository<UserTokenEntity, UUID> {
}
