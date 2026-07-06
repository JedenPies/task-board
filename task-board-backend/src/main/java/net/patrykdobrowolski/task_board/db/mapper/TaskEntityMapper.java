package net.patrykdobrowolski.task_board.db.mapper;

import net.patrykdobrowolski.task_board.db.entity.TaskEntity;
import net.patrykdobrowolski.task_board.domain.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskEntityMapper {

    Task fromEntity(TaskEntity entity);

    @Mapping(target = "taskBoard", ignore = true)
    TaskEntity toEntity(Task domain);
}
