package net.patrykdobrowolski.auth.domain.port.in;

import net.patrykdobrowolski.auth.domain.AuthenticateWithExternalProviderCommand;
import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.AuthenticationResult;
import net.patrykdobrowolski.auth.domain.exception.InvalidRefreshTokenException;
import net.patrykdobrowolski.auth.domain.exception.InvalidCredentialsException;

public interface AuthenticationUseCase {

    AuthenticationResult authenticate(AuthenticateWithPasswordCommand request) throws InvalidCredentialsException;
    AuthenticationResult authenticate(AuthenticateWithExternalProviderCommand command) throws Exception;
    AuthenticationResult refresh(String oldToken) throws InvalidRefreshTokenException;
    void logout(String oldToken) throws InvalidRefreshTokenException;
}
