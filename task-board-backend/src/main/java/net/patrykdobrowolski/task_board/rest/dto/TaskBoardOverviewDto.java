package net.patrykdobrowolski.task_board.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Jacksonized
@Builder @Getter
public class TaskBoardOverviewDto {

    private final UUID id;
    private final String name;
    private final Boolean isPublic;

    private final Boolean canEdit;
    private final Boolean canChangeVisibility;
}
