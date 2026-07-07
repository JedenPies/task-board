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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-boards")
@RequiredArgsConstructor
public class TaskBoardsResource {

    private final TaskBoardsService taskBoardsService;
    private final TaskBoardMapper taskBoardMapper;
    private final TaskDtoMapper taskDtoMapper;

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
        return taskDtoMapper.toDto(taskBoardsService.changeTaskStatus(boardId, taskId, newStatus));
    }

    @DeleteMapping("{boardId}/tasks/{taskId}")
    public TaskDto deleteTask(@PathVariable UUID boardId, @PathVariable UUID taskId) throws ObjectNotFoundException {
        return taskDtoMapper.toDto(taskBoardsService.deleteTask(boardId, taskId));
    }

}
