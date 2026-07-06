package net.patrykdobrowolski.task_board.db.mapper;

import net.patrykdobrowolski.task_board.db.entity.TaskBoardEntity;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = TaskEntityMapper.class)
public interface TaskBoardEntityMapper {

    TaskBoardEntity toEntity(TaskBoard taskBoard);
    TaskBoard fromEntity(TaskBoardEntity entity);

    @AfterMapping
    default void linkTasks(@MappingTarget TaskBoardEntity entity) {
        entity.getTasks().forEach(t -> t.linkBoard(entity));
    }
}
