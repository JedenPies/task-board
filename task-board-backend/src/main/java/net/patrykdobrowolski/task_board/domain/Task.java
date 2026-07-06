package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
public class Task {

    private final UUID id;

    private String title;

    @Setter
    private TaskStatus status;
}
