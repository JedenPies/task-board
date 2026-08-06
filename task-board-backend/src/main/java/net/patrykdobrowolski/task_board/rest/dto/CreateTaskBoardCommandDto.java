package net.patrykdobrowolski.task_board.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder @Getter
public class CreateTaskBoardCommandDto {

    @NotBlank(message = "cannot be blank")
    private final String name;
}
