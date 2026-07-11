package net.patrykdobrowolski.task_board.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@Builder @Jacksonized
public class TaskBoardDto {

    @NotNull(message = "cannot be null")
    private final UUID id;
    @NotBlank(message = "cannot be blank")
    private final String name;
    private final Boolean isPublic;
    private final List<@Valid TaskDto> tasks;
}
