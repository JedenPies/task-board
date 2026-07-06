package net.patrykdobrowolski.task_board.domain.exception;

import java.util.UUID;

public class ObjectNotFoundException extends ObjectLocatorException {

    private ObjectNotFoundException(String objectType, UUID objectId) {
        super(objectType, objectId);
    }

    public static ObjectNotFoundException of(String objectType, UUID objectId) {
        return new ObjectNotFoundException(objectType, objectId);
    }
}
