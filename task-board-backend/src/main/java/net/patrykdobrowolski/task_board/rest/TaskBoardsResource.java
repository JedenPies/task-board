package net.patrykdobrowolski.task_board.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import net.patrykdobrowolski.task_board.rest.mapper.TaskBoardMapper;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-boards")
@RequiredArgsConstructor
public class TaskBoardsResource {

    private final TaskBoardsService taskBoardsService;
    private final TaskBoardMapper taskBoardMapper;

    @GetMapping
    public List<TaskBoardDto> findAllBoards() {
        return taskBoardsService.findAllBoards().stream().map(taskBoardMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskBoardDto postNewBoard(@Valid @RequestBody TaskBoardDto taskBoardDto) throws ObjectAlreadyExistsException {
        TaskBoard taskBoard = taskBoardMapper.fromDto(taskBoardDto);
        TaskBoard created = taskBoardsService.createBoard(taskBoard);
        return taskBoardMapper.toDto(created);
    }
}
