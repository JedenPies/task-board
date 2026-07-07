package net.patrykdobrowolski.task_board.rest;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseTaskBoardService {

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<UUID, List<SseEmitter>>();

    public SseEmitter createConnection(UUID boardId) {

        SseEmitter emitter = new SseEmitter(900_000L);
        this.emitters.computeIfAbsent(boardId, k -> Collections.synchronizedList(new ArrayList<SseEmitter>())).add(emitter);
        emitter.onCompletion(() -> removeEmitter(boardId, emitter));
        emitter.onTimeout(() -> removeEmitter(boardId, emitter));
        emitter.onError((e) -> removeEmitter(boardId, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to board: " + boardId));
        } catch (IOException e) {
            removeEmitter(boardId, emitter);
        }
        return emitter;
    }

    @Async
    public void broadcastBoardChange(UUID boardId) {
        List<SseEmitter> boardEmitters = emitters.get(boardId);
        if (boardEmitters != null && !boardEmitters.isEmpty()) {
            synchronized (boardEmitters) {
                boardEmitters.removeIf(emitter -> !sendNotification(emitter));
            }
        }
    }

    private boolean sendNotification(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("REFRESH").data("refresh"));
            return true; // Sukces, połączenie jest żywe
        } catch (IOException e) {
            emitter.complete();
            return false; // Błąd, emiter kwalifikuje się do usunięcia przez removeIf
        }
    }

    private void removeEmitter(UUID boardId, SseEmitter emitter) {
        List<SseEmitter> boardEmitters = emitters.get(boardId);
        if (boardEmitters != null) {
            boardEmitters.remove(emitter);
        }
    }
}
