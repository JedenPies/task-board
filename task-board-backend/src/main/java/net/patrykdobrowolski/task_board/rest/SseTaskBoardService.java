package net.patrykdobrowolski.task_board.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.UserContext;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.service.TaskBoardsService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseTaskBoardService {

    private final TaskBoardsService taskBoardsService;
    private final UserContext userContext;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createConnection(UUID boardId) throws AccessDeniedException, ObjectNotFoundException {
        log.debug("Creating SSE connection for board: {}", boardId);
        TaskBoard board = taskBoardsService.findBoard(boardId);
        board.checkViewPermissions(userContext);

        SseEmitter emitter = new SseEmitter(300_000L);

        this.emitters.computeIfAbsent(boardId, k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);

        emitter.onCompletion(() -> removeEmitter(boardId, emitter));
        emitter.onTimeout(() -> removeEmitter(boardId, emitter));
        emitter.onError(e -> removeEmitter(boardId, emitter));
        sendNotification(emitter, "INIT", "init");
        return emitter;
    }

    @Async
    public void broadcastBoardChange(UUID boardId) {
        List<SseEmitter> boardEmitters = emitters.get(boardId);
        if (boardEmitters != null && !boardEmitters.isEmpty()) {
            synchronized (boardEmitters) {
                boardEmitters.forEach(e -> sendNotification(e, "REFRESH", "refresh"));
            }
        }
    }

    @Scheduled(fixedRate = 30_000)
    public void broadcastHeartbeat() {
        log.debug("Sent heartbeat to {} connections", totalEmittersCount());
        emitters.entrySet().removeIf(e -> {
            List<SseEmitter> list = e.getValue();
            synchronized (list) {
                list.removeIf(emitter -> !sendNotification(emitter, "HEARTBEAT", "heartbeat"));
                return list.isEmpty();
            }
        });
        log.debug("Currently there are {} connections", totalEmittersCount());
    }

    private int totalEmittersCount() {
        return emitters.values().stream().mapToInt(List::size).sum();
    }

    private boolean sendNotification(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
            return true;
        } catch (IOException e) {
            emitter.complete();
            return false;
        }
    }

    private void removeEmitter(UUID boardId, SseEmitter emitter) {
        log.debug("Removing SSE connection for board: {}", boardId);
        List<SseEmitter> boardEmitters = emitters.get(boardId);
        if (boardEmitters != null) {
            synchronized (boardEmitters) {
                boardEmitters.remove(emitter);
                if (boardEmitters.isEmpty()) {
                    emitters.remove(boardId);
                }
            }
        }
    }
}
