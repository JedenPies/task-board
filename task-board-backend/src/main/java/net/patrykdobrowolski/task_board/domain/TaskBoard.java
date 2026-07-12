package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;

import java.util.*;

@Builder
@Getter
public class TaskBoard {

    private static final long POSITION_GAP = 65536L;

    private final UUID id;

    private String name;
    private List<Task> tasks;

    @With
    private String owner;
    @Builder.Default
    @Setter
    private Boolean isPublic = false;

    public void addNewTask(Task task) throws ObjectAlreadyExistsException {
        if (taskById(task.getId()).isPresent()) throw ObjectAlreadyExistsException.of("Task", task.getId());
        task.setStatus(TaskStatus.TODO);
        Long max = tasks.stream()
                .map(Task::getPosition)
                .max(Long::compareTo)
                .orElse(0L);
        task.setPosition(max + POSITION_GAP);
        tasks.add(task);
    }

    public Task moveTask(UUID taskId, TaskStatus newStatus, UUID followingTaskId) throws ObjectNotFoundException, CannotMoveTaskException {
        Task taskToMove = taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        if (taskId.equals(followingTaskId)) throw new CannotMoveTaskException();
        Optional<Task> followingTask =
                followingTaskId != null
                        ? Optional.of(taskByIdAndStatus(followingTaskId, newStatus).orElseThrow(() -> ObjectNotFoundException.of("Task", followingTaskId)))
                        : Optional.empty();
        this.tasks.remove(taskToMove);
        taskToMove.setStatus(newStatus);
        this.tasks.sort(Comparator.comparing(Task::getPosition));

        int nextIndex = followingTask.map(tasks::indexOf).orElse(tasks.size());
        int prevIndex = nextIndex - 1; // skrajnie - moze byc -1

        Optional<Task> precedingTask = prevIndex < 0 ? Optional.empty() : Optional.of(tasks.get(prevIndex));

        long prevPosition = precedingTask.map(Task::getPosition).orElse(0L);
        long nextPosition = followingTask.map(Task::getPosition).orElse(prevPosition + POSITION_GAP * 2);
        long newPosition = (nextPosition + prevPosition) / 2;
        taskToMove.setPosition(newPosition);

        tasks.add(nextIndex, taskToMove);
        if (Math.abs(prevPosition - nextPosition) < 2) {
            long currentPos = POSITION_GAP;
            for (Task t : tasks) {
                t.setPosition(currentPos);
                currentPos += POSITION_GAP;
            }
        }
        return taskToMove;
    }

    public Task editTask(UUID taskId, UpdateTaskCommand updateTaskCommand) throws ObjectNotFoundException {
        Task task = taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        task.update(updateTaskCommand);
        return task;
    }
    public List<Task> getTasks() {
        return Collections.unmodifiableList(Optional.ofNullable(tasks).orElseGet(Collections::emptyList));
    }

    public Optional<Task> taskById(UUID taskId) {
        return tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst();
    }

    public Optional<Task> taskByIdAndStatus(UUID taskId, TaskStatus status) {
        return taskById(taskId).filter(task -> task.getStatus() == status);
    }

    public Task deleteTaskById(UUID taskId) throws ObjectNotFoundException {
        Task found = tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst().orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        tasks.remove(found);
        return found;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public boolean isAllowedToEdit(UserContext userContext) {
        return owner == null
                || Boolean.TRUE.equals(isPublic)
                || owner.equals(userContext.getUserName());
    }

    public void checkPublicFlagPermission(UserContext userContext) throws AccessDeniedException {
        if (owner == null || !owner.equals(userContext.getUserName())) {
            throw new AccessDeniedException();
        }
    }

    public void checkEditPermissions(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToEdit(userContext)) {
            throw new AccessDeniedException();
        }
    }

}
