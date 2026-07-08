package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.domain.UserToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTokensRepositoryService {

    private final UserTokensRepository userTokensRepository;
    private final EntityMapper entityMapper;

    public void save(UserToken userToken) {
        userTokensRepository.save(entityMapper.toEntity(userToken));
    }
}
