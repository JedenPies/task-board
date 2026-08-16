package net.patrykdobrowolski.auth.rest;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.UpdateUserCommand;
import net.patrykdobrowolski.auth.domain.exception.UserNotFoundException;
import net.patrykdobrowolski.auth.domain.port.in.UsersUseCase;
import net.patrykdobrowolski.auth.rest.dto.UpdateUserDetailsCommandDto;
import net.patrykdobrowolski.auth.rest.dto.UserDetailsDto;
import net.patrykdobrowolski.auth.rest.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersResource {

    private final UsersUseCase usersUseCase;
    private final DtoMapper dtoMapper;

    @GetMapping("{userId}")
    public UserDetailsDto getUserDetails(@PathVariable UUID userId) throws UserNotFoundException {
        return dtoMapper.toUserDetailsDto(usersUseCase.getUser(userId));
    }

    @DeleteMapping("{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID userId) throws UserNotFoundException {
        usersUseCase.deleteUser(userId);
    }

    @PatchMapping("{userId}")
    public UserDetailsDto updateUser(@PathVariable UUID userId, @RequestBody UpdateUserDetailsCommandDto updateCommand) throws UserNotFoundException {
        UpdateUserCommand updateUserCommand = dtoMapper.toUpdateUserCommand(updateCommand);
        return dtoMapper.toUserDetailsDto(usersUseCase.updateUser(userId, updateUserCommand));
    }
}
