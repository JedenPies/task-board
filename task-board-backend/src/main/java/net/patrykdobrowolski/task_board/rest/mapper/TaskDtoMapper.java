package net.patrykdobrowolski.task_board.rest.mapper;

import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.domain.UpdateTaskCommand;
import net.patrykdobrowolski.task_board.rest.dto.NewTaskDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
import net.patrykdobrowolski.task_board.rest.dto.UpdateTaskCommandDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskDtoMapper {

    TaskDto toDto(Task task);
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Task fromDto(NewTaskDto taskDto);

    UpdateTaskCommand fromDto(UpdateTaskCommandDto updateTaskCommandDto);
}
