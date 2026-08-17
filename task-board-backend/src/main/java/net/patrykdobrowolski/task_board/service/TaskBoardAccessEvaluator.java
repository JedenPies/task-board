package net.patrykdobrowolski.task_board.service;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.UserContext;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskBoardAccessEvaluator {

    private final UserContext userContext;

    public void checkManipulateTasksPermissions(TaskBoard taskBoard) throws AccessDeniedException {
        if (!isAllowedToMoveTask(taskBoard)) {
            throw new AccessDeniedException();
        }
    }

    public void checkPublicFlagPermission(TaskBoard taskBoard) throws AccessDeniedException {
        if (!isAllowedToChangeVisibility(taskBoard)) {
            throw new AccessDeniedException();
        }
    }

    public void checkEditPermissions(TaskBoard taskBoard) throws AccessDeniedException {
        if (!isAllowedToEdit(taskBoard)) {
            throw new AccessDeniedException();
        }
    }

    public void checkViewPermissions(TaskBoard taskBoard) throws AccessDeniedException {
        if (!isAllowedToView(taskBoard)) {
            throw new AccessDeniedException();
        }
    }

    public void checkDeletePermissions(TaskBoard taskBoard) throws AccessDeniedException {
        if (!isAllowedToDelete(taskBoard)) {
            throw new AccessDeniedException();
        }
    }

    public boolean isAllowedToEdit(TaskBoard taskBoard) {
        return taskBoard.getOwner() == null
                || taskBoard.getOwner().equals(userContext.getUserId());
    }

    public boolean isAllowedToChangeVisibility(TaskBoard taskBoard) {
        return taskBoard.getOwner() != null && taskBoard.getOwner().equals(userContext.getUserId());
    }

    public boolean isAllowedToView(TaskBoard taskBoard) {
        return taskBoard.getIsPublic() || isAllowedToEdit(taskBoard);
    }

    public boolean isAllowedToMoveTask(TaskBoard taskBoard) {
        return taskBoard.getIsPublic()  || isAllowedToEdit(taskBoard);
    }

    public boolean isAllowedToDelete(TaskBoard taskBoard) {
        return taskBoard.getOwner() != null && taskBoard.getOwner().equals(userContext.getUserId());
    }
}
