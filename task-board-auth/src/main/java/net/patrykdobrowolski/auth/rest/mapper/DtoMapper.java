package net.patrykdobrowolski.auth.rest.mapper;

import net.patrykdobrowolski.auth.domain.AuthenticationRequest;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.rest.dto.AuthenticationRequestDto;
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

    public abstract AuthenticationRequest toRequest(AuthenticationRequestDto authenticationRequestDto);

    @Mapping(target = "passwordEncoded", expression = "java(encoder.encode(newUserDto.getPassword()))")
    public abstract User fromDto(NewUserDto newUserDto);
}
