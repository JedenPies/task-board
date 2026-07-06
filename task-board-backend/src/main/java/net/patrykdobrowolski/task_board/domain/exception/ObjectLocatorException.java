package net.patrykdobrowolski.task_board.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ObjectLocatorException extends Exception {

    @Getter
    private final String objectType;

    @Getter
    private final UUID objectId;

}
