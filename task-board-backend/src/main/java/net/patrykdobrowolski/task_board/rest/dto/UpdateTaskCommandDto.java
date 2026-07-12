package net.patrykdobrowolski.task_board.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Builder
@RequiredArgsConstructor
@Jacksonized
@Getter
public class UpdateTaskCommandDto {

    private final String newTitle;
    private final String newDescription;
}
