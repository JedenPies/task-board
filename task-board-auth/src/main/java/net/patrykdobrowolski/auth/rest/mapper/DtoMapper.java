package net.patrykdobrowolski.auth.rest.mapper;

import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.rest.dto.AuthenticateCommandDto;
import net.patrykdobrowolski.auth.rest.dto.NewUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "passwordEncoded", expression = "java(encoder.encode(newUserDto.getPassword()))")
    @Mapping(target = "id", ignore = true)
    public abstract User fromDto(NewUserDto newUserDto);


}
