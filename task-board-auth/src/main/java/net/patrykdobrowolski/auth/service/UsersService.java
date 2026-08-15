package net.patrykdobrowolski.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.ExternalUserProfile;
import net.patrykdobrowolski.auth.domain.UpdateUserCommand;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.exception.UserNotFoundException;
import net.patrykdobrowolski.auth.domain.port.out.UsersRepository;
import net.patrykdobrowolski.auth.domain.port.in.UsersUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsersService implements UsersUseCase {

    private final UsersRepository usersRepositoryService;

    @Override
    @Transactional
    public User createNewUser(AuthProvider authProvider, ExternalUserProfile profile) {
        User newUser = User.builder()
                .authProvider(authProvider)
                .username(null) // username is not set for external users
                .providerId(profile.userId())
                .displayName(profile.name())
                .build();
        return usersRepositoryService.save(newUser);
    }

    @Override
    @Transactional
    @PreAuthorize("@usersSecurity.canViewUser(authentication, #userId)")
    public User getUser(UUID userId) throws UserNotFoundException {
        return usersRepositoryService.findByUserId(userId).orElseThrow(() -> UserNotFoundException.of(userId));
    }

    @Override
    @Transactional
    @PreAuthorize("@usersSecurity.canUpdateUser(authentication, #userId)")
    public User updateUser(UUID userId, UpdateUserCommand command) throws UserNotFoundException {
        User user = usersRepositoryService.findByUserId(userId).orElseThrow(() -> UserNotFoundException.of(userId));
        user.update(command);
        return usersRepositoryService.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("@usersSecurity.canDeleteUser(authentication, #userId)")
    public void deleteUser(UUID userId) throws UserNotFoundException {
        User userToDelete = usersRepositoryService.findByUserId(userId).orElseThrow(() -> UserNotFoundException.of(userId));
        userToDelete.delete();
        usersRepositoryService.save(userToDelete);
    }
}
