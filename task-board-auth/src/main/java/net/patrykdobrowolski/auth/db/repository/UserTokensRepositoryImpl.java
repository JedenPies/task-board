package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.domain.UserToken;
import net.patrykdobrowolski.auth.domain.port.out.UserTokensRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserTokensRepositoryImpl implements UserTokensRepository {

    private final SpringDataUserTokensRepository userTokensRepository;
    private final EntityMapper entityMapper;

    @Override
    public void save(UserToken userToken) {
        userTokensRepository.save(entityMapper.toEntity(userToken));
    }

    @Override
    public Optional<UserToken> findByRefreshToken(String refreshToken) {
        return userTokensRepository.findByRefreshToken(refreshToken).map(entityMapper::fromEntity);
    }
}
