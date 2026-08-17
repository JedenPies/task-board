package net.patrykdobrowolski.task_board.domain;

import lombok.AccessLevel;
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

    @Setter(AccessLevel.PACKAGE)
    private TaskStatus status;
    @Setter(AccessLevel.PACKAGE)
    private Long position;

    private boolean deleted;

    public void update(UpdateTaskCommand command) {
        this.title = command.newTitle();
        this.description = command.newDescription();
    }

    public void delete() {
        this.deleted = true;
    }
}
