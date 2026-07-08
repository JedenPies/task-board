package net.patrykdobrowolski.auth.rest;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.rest.dto.NewUserDto;
import net.patrykdobrowolski.auth.rest.mapper.DtoMapper;
import net.patrykdobrowolski.auth.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersResource {

    private final DtoMapper dtoMapper;
    private final UsersService usersService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewUser(@RequestBody NewUserDto user) {
        usersService.createNewUser(dtoMapper.fromDto(user));
    }
}
