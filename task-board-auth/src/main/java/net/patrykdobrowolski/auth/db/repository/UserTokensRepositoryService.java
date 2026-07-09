package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.domain.UserToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserTokensRepositoryService {

    private final UserTokensRepository userTokensRepository;
    private final EntityMapper entityMapper;

    public void save(UserToken userToken) {
        userTokensRepository.save(entityMapper.toEntity(userToken));
    }

    public Optional<UserToken> findByRefreshToken(String refreshToken) {
        return userTokensRepository.findByRefreshToken(refreshToken).map(entityMapper::fromEntity);
    }
}
