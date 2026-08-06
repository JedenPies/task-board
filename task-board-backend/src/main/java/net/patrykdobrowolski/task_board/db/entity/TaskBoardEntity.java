package net.patrykdobrowolski.task_board.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "task_boards")
@NoArgsConstructor @AllArgsConstructor
@Getter
@Builder
public class TaskBoardEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "taskBoard")
    private List<TaskEntity> tasks;

    private String owner;
    private Boolean isPublic;

}
