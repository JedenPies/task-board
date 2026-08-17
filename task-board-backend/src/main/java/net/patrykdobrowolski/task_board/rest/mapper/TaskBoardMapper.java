package net.patrykdobrowolski.task_board.rest.mapper;

import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.rest.dto.CreateTaskBoardCommandDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardOverviewDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
import net.patrykdobrowolski.task_board.service.TaskBoardAccessEvaluator;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;

@Mapper(componentModel = "spring", uses = TaskDtoMapper.class)
public abstract class TaskBoardMapper {

    @Autowired
    protected TaskBoardAccessEvaluator taskBoardAccess;

    @Mapping(target = "canEdit", expression = "java(taskBoardAccess.isAllowedToEdit(taskBoard))")
    @Mapping(target = "canChangeVisibility", expression = "java(taskBoardAccess.isAllowedToChangeVisibility(taskBoard))")
    public abstract TaskBoardDto toDto(TaskBoard taskBoard);

    @Mapping(target = "canEdit", expression = "java(taskBoardAccess.isAllowedToEdit(taskBoard))")
    @Mapping(target = "canChangeVisibility", expression = "java(taskBoardAccess.isAllowedToChangeVisibility(taskBoard))")
    public abstract TaskBoardOverviewDto toOverviewDto(TaskBoard taskBoard);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "isPublic", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract TaskBoard fromDto(CreateTaskBoardCommandDto taskBoardDto);

    @AfterMapping
    public TaskBoardDto sortTasks(@MappingTarget TaskBoardDto taskBoardDto) {
        taskBoardDto.getTasks().sort(Comparator.comparing(TaskDto::getPosition));
        return taskBoardDto;
    }
}
