package net.patrykdobrowolski.auth.db.repository;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.EntityMapper;
import net.patrykdobrowolski.auth.service.AuthProvider;
import net.patrykdobrowolski.auth.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersRepositoryService {

    private final UsersRepository usersRepository;
    private final EntityMapper entityMapper;

    public Optional<User> findByUsername(String username) {
        return usersRepository.findByUsername(username).map(entityMapper::fromEntity);
    }

    public Optional<User> findByExternalAuthProvider(AuthProvider authProvider, String providerId) {
        return usersRepository.findByAuthProviderAndProviderId(authProvider, providerId).map(entityMapper::fromEntity);
    }

    public User save(User user) {
        return entityMapper.fromEntity(usersRepository.save(entityMapper.toEntity(user)));
    }
}
