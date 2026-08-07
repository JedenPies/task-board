package net.patrykdobrowolski.task_board.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.*;
import net.patrykdobrowolski.task_board.rest.mapper.TaskBoardMapper;
import net.patrykdobrowolski.task_board.rest.mapper.TaskDtoMapper;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task-boards/{boardId}/tasks")
@RequiredArgsConstructor
public class TasksResource {

    private final TaskDtoMapper taskDtoMapper;
    private final TaskBoardsService taskBoardsService;
    private final SseTaskBoardService sseTaskBoardService;
    private final TaskBoardMapper taskBoardMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto addNewTask(@PathVariable UUID boardId, @Valid @RequestBody NewTaskDto taskDto) throws ObjectNotFoundException, ObjectAlreadyExistsException, AccessDeniedException {
        Task task = taskDtoMapper.fromDto(taskDto);
        Task result = taskBoardsService.addTaskToBoard(boardId, task);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(result);
    }

    @PostMapping("/{taskId}/update-task-requests")
    public TaskDto editTask(@PathVariable UUID boardId, @PathVariable UUID taskId, @RequestBody UpdateTaskCommandDto updateTaskCommandDto) throws ObjectNotFoundException, AccessDeniedException {
        Task task = taskBoardsService.editTask(boardId, taskId, taskDtoMapper.fromDto(updateTaskCommandDto));
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }

    @GetMapping("/{taskId}")
    public TaskDto findTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        return taskDtoMapper.toDto(taskBoardsService.findTask(boardId, taskId));
    }

    @DeleteMapping("/{taskId}")
    public TaskDto deleteTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        Task task = taskBoardsService.deleteTask(boardId, taskId);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }

    @PutMapping("/{taskId}/position")
    @Deprecated
    public TaskBoardDto moveTask(
            @PathVariable UUID boardId, @PathVariable UUID taskId, @RequestBody MoveTaskCommandDto positionDto) throws ObjectNotFoundException, AccessDeniedException, CannotMoveTaskException {
        TaskBoard board = taskBoardsService.moveTask(boardId, taskId, positionDto.getFollowingTaskId(), positionDto.getStatus());
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskBoardMapper.toDto(board);
    }

}
