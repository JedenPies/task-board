package net.patrykdobrowolski.task_board.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import net.patrykdobrowolski.task_board.domain.TaskStatus;

import java.util.UUID;

@Getter
@Builder @Jacksonized
public class TaskStatusAndPositionDto {

    private final TaskStatus status;
    private final UUID followingTaskId;
}
