package net.patrykdobrowolski.auth.db.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import net.patrykdobrowolski.auth.db.entity.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokensRepository extends JpaRepository<UserTokenEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<UserTokenEntity> findByRefreshToken(String refreshToken);
}
