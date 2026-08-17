package net.patrykdobrowolski.task_board.domain;

import java.util.UUID;

public interface UserContext {

    UUID getUserId();

    default boolean isLoggedIn() {
        return getUserId() != null;
    }
}
