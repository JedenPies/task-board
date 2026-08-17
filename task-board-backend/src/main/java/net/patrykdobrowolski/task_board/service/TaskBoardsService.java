package net.patrykdobrowolski.task_board.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.db.TaskBoardsRepositoryService;
import net.patrykdobrowolski.task_board.domain.*;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskBoardsService {

    private final TaskBoardsRepositoryService repositoryService;
    private final UserContext userContext;
    private final TaskBoardAccessEvaluator taskBoardAccess;

    @Transactional
    public List<TaskBoard> findAllBoards() {
        return userContext.isLoggedIn()
                ? repositoryService.findAllBoards(userContext.getUserId())
                : Collections.emptyList();
    }

    @Transactional
    public Task addTaskToBoard(UUID boardId, Task task) throws ObjectNotFoundException, ObjectAlreadyExistsException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("board", boardId));
        taskBoardAccess.checkManipulateTasksPermissions(board);
        Task added = board.addNewTask(task);
        repositoryService.save(board);
        return added;
    }

    @Transactional
    public TaskBoard createBoard(TaskBoard taskBoard) {
        if (!userContext.isLoggedIn()) {
            taskBoard.setIsPublic(true);
        }
        return repositoryService.save(taskBoard.withOwner(userContext.getUserId()));
    }

    @Transactional
    public TaskBoard findBoard(UUID boardId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard found = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkViewPermissions(found);
        return found;
    }

    @Transactional
    public Task findTask(UUID boardId, UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkViewPermissions(board);
        return board.taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
    }

    @Transactional
    public Task deleteTask(UUID boardId, UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkManipulateTasksPermissions(board);
        Task task = board.deleteTaskById(taskId);
        repositoryService.save(board);
        return task;
    }

    @Transactional
    public TaskBoard changeName(UUID boardId, String newName) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkEditPermissions(board);
        board.changeName(newName);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public TaskBoard setPublicFlag(UUID boardId, Boolean isPublic) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkPublicFlagPermission(board);
        board.setIsPublic(isPublic);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public TaskBoard moveTask(UUID boardId, UUID taskId, UUID followingTaskId, TaskStatus newStatus) throws ObjectNotFoundException, AccessDeniedException, CannotMoveTaskException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkManipulateTasksPermissions(board);
        board.moveTask(taskId, newStatus, followingTaskId);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public Task editTask(UUID boardId, UUID taskId, UpdateTaskCommand updateTaskCommand) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkManipulateTasksPermissions(board);
        Task edited = board.editTask(taskId, updateTaskCommand);
        repositoryService.save(board);
        return edited;
    }

    @Transactional
    public void deleteBoard(UUID boardId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        taskBoardAccess.checkDeletePermissions(board);
        board.delete();
        repositoryService.save(board);
    }
}
