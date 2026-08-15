package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.port.out.UsersRepository;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersRepositoryImpl implements UsersRepository {

    private final SpringDataUsersRepository usersRepository;
    private final EntityMapper entityMapper;

    @Override
    public Optional<User> findByUsername(String username) {
        return usersRepository.findByUsername(username).map(entityMapper::fromEntity);
    }

    @Override
    public Optional<User> findByUserId(UUID userId) {
        return usersRepository.findById(userId).map(entityMapper::fromEntity);
    }

    @Override
    public Optional<User> findByExternalAuthProvider(AuthProvider authProvider, String providerId) {
        return usersRepository.findByAuthProviderAndProviderId(authProvider, providerId).map(entityMapper::fromEntity);
    }

    @Override
    public User save(User user) {
        return entityMapper.fromEntity(usersRepository.save(entityMapper.toEntity(user)));
    }
}
