package net.patrykdobrowolski.task_board.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.UUID;

@Entity(name = "task_boards")
@NoArgsConstructor @AllArgsConstructor
@Getter
@Builder
@SQLRestriction("deleted = false")
public class TaskBoardEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "taskBoard")
    @SQLRestriction("deleted = false")
    private List<TaskEntity> tasks;

    private UUID owner;
    private Boolean isPublic;

    private boolean deleted;

}
