package net.patrykdobrowolski.task_board.domain;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

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

    private boolean deleted;

    public Task addNewTask(Task task) throws ObjectAlreadyExistsException {
        if (taskById(task.getId()).isPresent()) throw ObjectAlreadyExistsException.of("Task", task.getId());
        task.setStatus(Optional.ofNullable(task.getStatus()).orElse(TaskStatus.TODO));
        Optional<Task> firstTask = tasks.stream()
                .filter(t -> t.getStatus() == task.getStatus())
                .min(Comparator.comparing(Task::getPosition));
        insertBefore(task, firstTask.orElse(null));
        return task;
    }

    public void moveTask(UUID taskId, TaskStatus newStatus, @Nullable UUID followingTaskId) throws ObjectNotFoundException, CannotMoveTaskException {

        Task taskToMove = taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        this.tasks.remove(taskToMove);
        if (taskId.equals(followingTaskId)) throw new CannotMoveTaskException();
        taskToMove.setStatus(newStatus);
        Task followingTask = taskByIdAndStatus(followingTaskId, newStatus).orElse(null);
        insertBefore(taskToMove, followingTask);
    }

    private void insertBefore(Task newTask, @Nullable Task followingTask) {
        List<Task> tasks = this.tasks.stream().filter(t -> t.getStatus() == newTask.getStatus())
                .sorted(Comparator.comparing(Task::getPosition))
                .collect(Collectors.toCollection(ArrayList::new));

        Optional<Task> followingTaskOpt = Optional.ofNullable(followingTask);
        Optional<Task> precedingTaskOpt;
        if (followingTaskOpt.isEmpty()) {
            precedingTaskOpt = tasks.isEmpty() ? Optional.empty() : Optional.of(tasks.getLast());
        } else {
            int foll = tasks.indexOf(followingTaskOpt.get());
            precedingTaskOpt = foll > 0 ? Optional.of(tasks.get(foll - 1)) : Optional.empty();
        }

        Long precedingTaskPosition = precedingTaskOpt.map(Task::getPosition).orElse(0L);
        Long followingTaskPosition = followingTaskOpt.map(Task::getPosition).orElse(precedingTaskPosition + POSITION_GAP * 2);
        Long newTaskPosition = (precedingTaskPosition + followingTaskPosition) / 2;
        newTask.setPosition(newTaskPosition);
        if (followingTaskPosition - precedingTaskPosition <= 1) {
            tasks.add(newTask);
            rebalance(tasks);
        }
        this.tasks.add(newTask);
    }

    private void rebalance(List<Task> tasksToRebalance) {
       long currentPos = POSITION_GAP;
       for (Task t : tasksToRebalance) {
            t.setPosition(currentPos);
            currentPos += POSITION_GAP;
        }
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
        found.delete();
        return found;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isAllowedToEdit(UserContext userContext) {
        return owner == null
                || owner.equals(userContext.getUserName());
    }

    public boolean isAllowedToChangeVisibility(UserContext userContext) {
        return owner != null && owner.equals(userContext.getUserName());
    }

    public boolean isAllowedToView(UserContext userContext) {
        return isPublic || isAllowedToEdit(userContext);
    }

    public boolean isAllowedToMoveTask(UserContext userContext) {
        return isPublic || isAllowedToEdit(userContext);
    }

    public boolean isAllowedToDelete(UserContext userContext) {
        return owner.equals(userContext.getUserName());
    }

    public void checkPublicFlagPermission(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToChangeVisibility(userContext)) {
            throw new AccessDeniedException();
        }
    }

    public void checkEditPermissions(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToEdit(userContext)) {
            throw new AccessDeniedException();
        }
    }

    public void checkViewPermissions(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToView(userContext)) {
            throw new AccessDeniedException();
        }
    }

    public void checkManipulateTasksPermissions(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToMoveTask(userContext)) {
            throw new AccessDeniedException();
        }
    }

    public void checkDeletePermissions(UserContext userContext) throws AccessDeniedException {
        if (!isAllowedToDelete(userContext)) {
            throw new AccessDeniedException();
        }
    }
}
