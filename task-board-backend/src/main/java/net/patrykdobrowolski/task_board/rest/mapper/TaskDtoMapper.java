package net.patrykdobrowolski.task_board.rest.mapper;

import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskDtoMapper {

    TaskDto toDto(Task task);
    Task fromDto(TaskDto taskDto);
}
