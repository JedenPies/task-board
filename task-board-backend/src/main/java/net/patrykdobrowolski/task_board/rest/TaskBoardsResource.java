package net.patrykdobrowolski.task_board.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.TaskStatus;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import net.patrykdobrowolski.task_board.rest.dto.TaskDto;
import net.patrykdobrowolski.task_board.rest.mapper.TaskBoardMapper;
import net.patrykdobrowolski.task_board.rest.mapper.TaskDtoMapper;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.SequencedSet;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-boards")
@RequiredArgsConstructor
public class TaskBoardsResource {

    private final TaskBoardsService taskBoardsService;
    private final TaskBoardMapper taskBoardMapper;
    private final TaskDtoMapper taskDtoMapper;
    private final SseTaskBoardService sseTaskBoardService;

    @GetMapping
    public List<TaskBoardDto> findAllBoards() {
        return taskBoardsService.findAllBoards().stream().map(taskBoardMapper::toDto).toList();
    }

    @GetMapping("/{boardId}")
    public TaskBoardDto getBoard(@PathVariable UUID boardId) throws ObjectNotFoundException {
        return taskBoardMapper.toDto(taskBoardsService.findBoard(boardId));
    }

    @PostMapping("/{boardId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto addNewTask(@PathVariable UUID boardId, @Valid @RequestBody TaskDto taskDto) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Task task = taskDtoMapper.fromDto(taskDto);
        Task result = taskBoardsService.addTaskToBoard(boardId, task);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskBoardDto postNewBoard(@Valid @RequestBody TaskBoardDto taskBoardDto) throws ObjectAlreadyExistsException {
        TaskBoard taskBoard = taskBoardMapper.fromDto(taskBoardDto);
        TaskBoard created = taskBoardsService.createBoard(taskBoard);
        return taskBoardMapper.toDto(created);
    }

    @GetMapping("{boardId}/tasks/{taskId}")
    public TaskDto findTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException {
        return taskDtoMapper.toDto(taskBoardsService.findTask(boardId, taskId));
    }


    @PutMapping("{boardId}/tasks/{taskId}/status")
    public TaskDto changeStatus(@PathVariable UUID boardId, @PathVariable UUID taskId, @RequestBody TaskStatus newStatus) throws ObjectNotFoundException {
        Task task = taskBoardsService.changeTaskStatus(boardId, taskId, newStatus);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }

    @DeleteMapping("{boardId}/tasks/{taskId}")
    public TaskDto deleteTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException {
        Task task = taskBoardsService.deleteTask(boardId, taskId);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskDtoMapper.toDto(task);
    }

    @GetMapping(value = "{boardId}/sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBoardEvents(@PathVariable UUID boardId) {
        return sseTaskBoardService.createConnection(boardId);
    }
}
