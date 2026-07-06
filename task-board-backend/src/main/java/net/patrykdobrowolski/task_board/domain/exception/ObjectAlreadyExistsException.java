package net.patrykdobrowolski.task_board.domain.exception;

import java.util.UUID;

public class ObjectAlreadyExistsException extends ObjectLocatorException {

    private ObjectAlreadyExistsException(String objectType, UUID objectId) {
        super(objectType, objectId);
    }

    public static ObjectAlreadyExistsException of(String objectType, UUID objectId) {
        return new ObjectAlreadyExistsException(objectType, objectId);
    }
}
