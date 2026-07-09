package net.patrykdobrowolski.task_board.db;

import net.patrykdobrowolski.task_board.db.entity.TaskBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskBoardsRepository extends JpaRepository<TaskBoardEntity, UUID> {

    List<TaskBoardEntity> findAllByOwner(String owner);
}
