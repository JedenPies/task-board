package net.patrykdobrowolski.task_board.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.domain.TaskStatus;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.NewTaskDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto addNewTask(@PathVariable UUID boardId, @Valid @RequestBody NewTaskDto taskDto) throws ObjectNotFoundException, ObjectAlreadyExistsException, AccessDeniedException {
        Task task = taskDtoMapper.fromDto(taskDto);
        Task result = taskBoardsService.addTaskToBoard(boardId, task);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(result);
    }

    @GetMapping("/{taskId}")
    public TaskDto findTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        return taskDtoMapper.toDto(taskBoardsService.findTask(boardId, taskId));
    }

    @PutMapping("/{taskId}/status")
    public TaskDto changeStatus(@PathVariable UUID boardId, @PathVariable UUID taskId, @RequestBody TaskStatus newStatus) throws ObjectNotFoundException, AccessDeniedException, CannotMoveTaskException {
        Task task = taskBoardsService.changeTaskStatus(boardId, taskId, newStatus);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }

    @DeleteMapping("/{taskId}")
    public TaskDto deleteTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        Task task = taskBoardsService.deleteTask(boardId, taskId);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }
}
