package net.patrykdobrowolski.task_board.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder @Jacksonized
public class TaskDto {

    @NotNull(message = "cannot be null")
    private final UUID id;
    @NotBlank(message = "cannot be empty")
    private final String title;
    private final String status;
}
