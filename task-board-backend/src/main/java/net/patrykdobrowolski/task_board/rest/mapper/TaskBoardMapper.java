package net.patrykdobrowolski.task_board.rest.mapper;

import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TaskDtoMapper.class)
public interface TaskBoardMapper {

    TaskBoardDto toDto(TaskBoard taskBoard);
    @Mapping(target = "owner", ignore = true)
    TaskBoard fromDto(TaskBoardDto taskBoardDto);
}
