package net.patrykdobrowolski.task_board.db.entity;

import jakarta.persistence.*;
import lombok.*;
import net.patrykdobrowolski.task_board.domain.TaskStatus;

import java.util.UUID;

@Entity(name = "tasks")
@NoArgsConstructor @AllArgsConstructor
@Getter
@Builder
public class TaskEntity {

    @Id
    private UUID id;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private Long position;

    @ManyToOne
    @JoinColumn(name = "task_board_id")
    private TaskBoardEntity taskBoard;

    public void linkBoard(TaskBoardEntity taskBoard) {
        this.taskBoard = taskBoard;
    }
}
