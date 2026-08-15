package net.patrykdobrowolski.auth.domain.port.out;

import net.patrykdobrowolski.auth.domain.UserToken;

import java.util.Optional;

public interface UserTokensRepository {

    void save(UserToken userToken);
    Optional<UserToken> findByRefreshToken(String refreshToken);
}
