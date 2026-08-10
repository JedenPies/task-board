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
    private String description;

    @Setter
    private TaskStatus status;
    @Setter
    private Long position;

    private boolean deleted;

    public void update(UpdateTaskCommand command) {
        this.title = command.getNewTitle();
        this.description = command.getNewDescription();
    }

    public void delete() {
        this.deleted = true;
    }
}
