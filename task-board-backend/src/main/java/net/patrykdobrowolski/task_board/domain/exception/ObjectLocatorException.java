package net.patrykdobrowolski.task_board.domain.exception;

import lombok.Getter;

import java.util.UUID;

public class ObjectLocatorException extends Exception {

    @Getter
    private final String objectType;

    @Getter
    private final UUID objectId;

    public ObjectLocatorException(String objectType, UUID objectId) {
        super(String.format("Object of type %s with id %s not found", objectType, objectId));
        this.objectType = objectType;
        this.objectId = objectId;
    }
}
