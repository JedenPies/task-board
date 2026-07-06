package net.patrykdobrowolski.task_board.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.task_board.db.TaskBoardsRepositoryService;
import net.patrykdobrowolski.task_board.domain.Task;
import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.TaskStatus;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskBoardsService {

    private final TaskBoardsRepositoryService repositoryService;

    public List<TaskBoard> findAllBoards() {
        return repositoryService.findAllBoards();
    }

    @Transactional
    public Task addTaskToBoard(UUID boardId, Task task) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("board", boardId));
        if (board.taskById(task.getId()).isPresent()) throw ObjectAlreadyExistsException.of("Task", task.getId());
        task.setStatus(TaskStatus.TODO);
        board.addTask(task);
        repositoryService.save(board);
        return task;
    }

    @Transactional
    public TaskBoard createBoard(TaskBoard taskBoard) throws ObjectAlreadyExistsException {
        if (repositoryService.findById(taskBoard.getId()).isPresent()) {
            throw ObjectAlreadyExistsException.of("Board", taskBoard.getId());
        }
        return repositoryService.save(taskBoard);
    }

    @Transactional
    public Task changeTaskStatus(UUID boardId, UUID taskId, TaskStatus newStatus) throws ObjectNotFoundException {
        TaskBoard board = findBoard(boardId);
        Task task = board.taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        task.setStatus(newStatus);
        repositoryService.save(board);
        return task;
    }

    @Transactional
    public TaskBoard findBoard(UUID boardId) throws ObjectNotFoundException {
        return repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
    }

    @Transactional
    public Task findTask(UUID boardId, UUID taskId) throws ObjectNotFoundException {
        TaskBoard board = repositoryService.findById(boardId).orElseThrow(() -> ObjectNotFoundException.of("Board", boardId));
        return board.taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
    }
}
