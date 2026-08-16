package net.patrykdobrowolski.auth.rest.mapper;

import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.UpdateUserCommand;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.rest.dto.AuthenticateCommandDto;
import net.patrykdobrowolski.auth.rest.dto.UpdateUserDetailsCommandDto;
import net.patrykdobrowolski.auth.rest.dto.UserDetailsDto;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class DtoMapper {

    protected PasswordEncoder encoder;

    @Autowired
    public void setEncoder(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public abstract AuthenticateWithPasswordCommand toRequest(AuthenticateCommandDto authenticateCommandDto);
    public abstract UpdateUserCommand toUpdateUserCommand(UpdateUserDetailsCommandDto updateUserDetailsCommandDto);

    public abstract UserDetailsDto toUserDetailsDto(User user);
}
