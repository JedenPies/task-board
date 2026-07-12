package net.patrykdobrowolski.task_board.rest.mapper;

import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Comparator;

@Mapper(componentModel = "spring", uses = TaskDtoMapper.class)
public interface TaskBoardMapper {

    TaskBoardDto toDto(TaskBoard taskBoard);
    @Mapping(target = "owner", ignore = true)
    TaskBoard fromDto(TaskBoardDto taskBoardDto);

    @AfterMapping
    default TaskBoardDto sortTasks(@MappingTarget TaskBoardDto taskBoardDto) {
        taskBoardDto.getTasks().sort(Comparator.comparing(TaskDto::getPosition));
        return taskBoardDto;
    }
}
