package net.patrykdobrowolski.task_board.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder @Getter
public class ChangeNameCommandDto {

    private final String newName;
}
