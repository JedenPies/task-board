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

    public List<TaskBoard> findAllBoards() {
        return userContext.isNotLoggedIn()
                ? Collections.emptyList()
                : repositoryService.findAllBoards(userContext.getUserName());
    }

    @Transactional
    public Task addTaskToBoard(UUID boardId, Task task) throws ObjectNotFoundException, ObjectAlreadyExistsException, AccessDeniedException, CannotMoveTaskException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("board", boardId));
        board.checkEditPermissions(userContext);
        Task added = board.addNewTask(task);
        repositoryService.save(board);
        return added;
    }

    @Transactional
    public TaskBoard createBoard(TaskBoard taskBoard) throws ObjectAlreadyExistsException {
        if (repositoryService.findById(taskBoard.getId()).isPresent()) {
            throw ObjectAlreadyExistsException.of("Board", taskBoard.getId());
        }
        return repositoryService.save(taskBoard.withOwner(userContext.getUserName()));
    }

    @Transactional
    public Task changeTaskStatus(UUID boardId, UUID taskId, TaskStatus newStatus) throws ObjectNotFoundException, AccessDeniedException, CannotMoveTaskException {
        TaskBoard board = findBoard(boardId);
        board.checkEditPermissions(userContext);
        Task task = board.moveTask(taskId, newStatus, null);
        repositoryService.save(board);
        return task;
    }

    @Transactional
    public TaskBoard findBoard(UUID boardId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard found = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        found.checkEditPermissions(userContext);
        return found;
    }

    @Transactional
    public Task findTask(UUID boardId, UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkEditPermissions(userContext);
        return board.taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
    }

    @Transactional
    public Task deleteTask(UUID boardId, UUID taskId) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkEditPermissions(userContext);
        Task task = board.deleteTaskById(taskId);
        repositoryService.save(board);
        return task;
    }

    @Transactional
    public TaskBoard changeName(UUID boardId, String newName) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkEditPermissions(userContext);
        board.changeName(newName);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public TaskBoard setPublicFlag(UUID boardId, Boolean isPublic) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkPublicFlagPermission(userContext);
        board.setIsPublic(isPublic);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public TaskBoard moveTask(UUID boardId, UUID taskId, UUID followingTaskId, TaskStatus newStatus) throws ObjectNotFoundException, AccessDeniedException, CannotMoveTaskException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkEditPermissions(userContext);
        board.moveTask(taskId, newStatus, followingTaskId);
        repositoryService.save(board);
        return board;
    }

    @Transactional
    public Task editTask(UUID boardId, UUID taskId, UpdateTaskCommand updateTaskCommand) throws ObjectNotFoundException, AccessDeniedException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        board.checkEditPermissions(userContext);
        Task edited = board.editTask(taskId, updateTaskCommand);
        repositoryService.save(board);
        return edited;
    }
}
