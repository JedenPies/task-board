package net.patrykdobrowolski.task_board.db;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.db.entity.TaskBoardEntity;
import net.patrykdobrowolski.task_board.db.mapper.TaskBoardEntityMapper;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskBoardsRepositoryService {

    private final TaskBoardsRepository repository;
    private final TaskBoardEntityMapper mapper;

    public List<TaskBoard> findAllBoards() {
        return repository.findAll().stream().map(mapper::fromEntity).toList();
    }

    public Optional<TaskBoard> findById(UUID id) {
        return repository.findById(id).map(mapper::fromEntity);
    }

    public TaskBoard save(TaskBoard taskBoard) {
        TaskBoardEntity entity = mapper.toEntity(taskBoard);
        return mapper.fromEntity(repository.save(entity));
    }
}
