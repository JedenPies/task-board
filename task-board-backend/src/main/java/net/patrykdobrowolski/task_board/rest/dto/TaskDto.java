package net.patrykdobrowolski.task_board.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import net.patrykdobrowolski.task_board.domain.TaskStatus;

import java.util.UUID;

@Getter
@Builder @Jacksonized
public class TaskDto {

    @NotNull(message = "cannot be null")
    private final UUID id;
    @NotBlank(message = "cannot be empty")
    @Size(max = 255, message = "max length is 255")
    private final String title;
    @Size(max = 2000, message = "max length is 2000")
    private final String description;
    private final TaskStatus status;
}
