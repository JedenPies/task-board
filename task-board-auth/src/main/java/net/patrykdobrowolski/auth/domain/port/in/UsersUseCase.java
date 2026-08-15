package net.patrykdobrowolski.auth.domain.port.in;

import net.patrykdobrowolski.auth.domain.UpdateUserCommand;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.exception.UserNotFoundException;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.ExternalUserProfile;

import java.util.UUID;

public interface UsersUseCase {

    User createNewUser(AuthProvider authProvider, ExternalUserProfile profile);
    User getUser(UUID userId) throws UserNotFoundException;
    User updateUser(UUID userId, UpdateUserCommand command) throws UserNotFoundException;
    void deleteUser(UUID userId) throws UserNotFoundException;
}
