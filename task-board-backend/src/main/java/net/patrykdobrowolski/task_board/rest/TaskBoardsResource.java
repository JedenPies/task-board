package net.patrykdobrowolski.task_board.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.*;
import net.patrykdobrowolski.task_board.rest.mapper.TaskBoardMapper;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-boards")
@RequiredArgsConstructor
public class TaskBoardsResource {

    private final TaskBoardsService taskBoardsService;
    private final TaskBoardMapper taskBoardMapper;
    private final SseTaskBoardService sseTaskBoardService;

    @GetMapping
    public List<TaskBoardOverviewDto> findAllBoards() {
        return taskBoardsService.findAllBoards().stream().map(taskBoardMapper::toOverviewDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskBoardOverviewDto postNewBoard(@Valid @RequestBody CreateTaskBoardCommandDto taskBoardDto) {
        TaskBoard taskBoard = taskBoardMapper.fromDto(taskBoardDto);
        TaskBoard created = taskBoardsService.createBoard(taskBoard);
        return taskBoardMapper.toOverviewDto(created);
    }

    @GetMapping(value = "/{boardId}")
    public TaskBoardDto getBoard(@PathVariable UUID boardId) throws ObjectNotFoundException, AccessDeniedException {
        return taskBoardMapper.toDto(taskBoardsService.findBoard(boardId));
    }


    @PutMapping("/{boardId}/name")
    public TaskBoardDto changeBoardName(@PathVariable UUID boardId, @RequestBody ChangeNameCommandDto command) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard result = taskBoardsService.changeName(boardId, command.getNewName());
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskBoardMapper.toDto(result);
    }

    @PutMapping("/{boardId}/visibility")
    public TaskBoardDto setPublicFlag(@PathVariable UUID boardId, @RequestBody ChangeVisibilityCommandDto command) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard result = taskBoardsService.setPublicFlag(boardId, command.getIsPublic());
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskBoardMapper.toDto(result);
    }

    @GetMapping(value = "/{boardId}/sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBoardEvents(@PathVariable UUID boardId) {
        return sseTaskBoardService.createConnection(boardId);
    }


}
