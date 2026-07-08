package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersRepositoryService {

    private final UsersRepository usersRepository;
    private final EntityMapper entityMapper;

    public Optional<User> findByLogin(String login) {
        return usersRepository.findByLogin(login).map(entityMapper::fromEntity);
    }

    public User save(User user) {
        return entityMapper.fromEntity(usersRepository.save(entityMapper.toEntity(user)));
    }
}
