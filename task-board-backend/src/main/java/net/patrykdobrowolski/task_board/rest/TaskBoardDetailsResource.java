package net.patrykdobrowolski.task_board.rest;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.TaskBoardDto;
import net.patrykdobrowolski.task_board.rest.mapper.TaskBoardMapper;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/task-boards/{boardId}")
@RequiredArgsConstructor
public class TaskBoardDetailsResource {

    private final TaskBoardsService taskBoardsService;
    private final SseTaskBoardService sseTaskBoardService;
    private final TaskBoardMapper taskBoardMapper;

    @PutMapping("/name")
    public TaskBoardDto changeBoardName(@PathVariable UUID boardId, @RequestBody String newName) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard result = taskBoardsService.changeName(boardId, newName);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskBoardMapper.toDto(result);
    }

    @PutMapping("/public")
    public TaskBoardDto setPublicFlag(@PathVariable UUID boardId, @RequestBody Boolean isPublic) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard result = taskBoardsService.setPublicFlag(boardId, isPublic);
        sseTaskBoardService.broadcastBoardChange(boardId);
        return taskBoardMapper.toDto(result);
    }

    @GetMapping("/")
    public TaskBoardDto getBoard(@PathVariable UUID boardId) throws ObjectNotFoundException, AccessDeniedException {
        return taskBoardMapper.toDto(taskBoardsService.findBoard(boardId));
    }

    @GetMapping(value = "/sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBoardEvents(@PathVariable UUID boardId) {
        return sseTaskBoardService.createConnection(boardId);
    }
}
