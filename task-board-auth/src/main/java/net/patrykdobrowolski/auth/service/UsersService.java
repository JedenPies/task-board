package net.patrykdobrowolski.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.db.repository.UsersRepositoryService;
import net.patrykdobrowolski.auth.domain.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepositoryService usersRepositoryService;

    @Transactional
    public User createNewUser(User user) {
        return usersRepositoryService.save(user);
    }

    @Transactional
    public User createNewUser(AuthProvider authProvider, ExternalUserProfile profile) {
        User newUser = User.builder()
                .authProvider(authProvider)
                .username(profile.username())
                .providerId(profile.userId())
                .email(profile.email())
                .displayName(profile.name())
                .build();
        return usersRepositoryService.save(newUser);
    }
}
