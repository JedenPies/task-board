package net.patrykdobrowolski.task_board.domain;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
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
    private UUID owner;
    @Builder.Default
    @Setter
    private Boolean isPublic = false;

    private boolean deleted;


    public void changeName(String newName) {
        this.name = newName;
    }

    public void delete() {
        this.deleted = true;
    }

    public Task addNewTask(Task task) throws ObjectAlreadyExistsException {
        if (taskById(task.getId()).isPresent()) throw ObjectAlreadyExistsException.of("Task", task.getId());
        task.setStatus(Optional.ofNullable(task.getStatus()).orElse(TaskStatus.TODO));
        Optional<Task> firstTask = Optional.ofNullable(tasks).orElseGet(Collections::emptyList).stream()
                .filter(t -> t.getStatus() == task.getStatus())
                .min(Comparator.comparing(Task::getPosition));
        insertBefore(task, firstTask.orElse(null));
        return task;
    }

    public void moveTask(UUID taskId, TaskStatus newStatus, @Nullable UUID followingTaskId) throws ObjectNotFoundException, CannotMoveTaskException {

        Task taskToMove = taskById(taskId).orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        Optional.ofNullable(this.tasks).ifPresent(t -> t.remove(taskToMove));
        if (taskId.equals(followingTaskId)) throw new CannotMoveTaskException();
        taskToMove.setStatus(newStatus);
        Task followingTask = followingTaskId == null
                ? null
                : taskByIdAndStatus(followingTaskId, newStatus).orElseThrow(() -> ObjectNotFoundException.of("Task", followingTaskId));
        insertBefore(taskToMove, followingTask);
    }

    private void insertBefore(Task newTask, @Nullable Task followingTask) {
        List<Task> tasks = Optional.ofNullable(this.tasks).orElseGet(Collections::emptyList).stream().filter(t -> t.getStatus() == newTask.getStatus())
                .sorted(Comparator.comparing(Task::getPosition))
                .collect(Collectors.toCollection(ArrayList::new));

        Optional<Task> followingTaskOpt = Optional.ofNullable(followingTask);
        Optional<Task> precedingTaskOpt;
        int followingIndex;
        if (followingTaskOpt.isEmpty()) {
            followingIndex = tasks.size();
            precedingTaskOpt = tasks.isEmpty() ? Optional.empty() : Optional.of(tasks.getLast());
        } else {
            followingIndex = tasks.indexOf(followingTaskOpt.get());
            precedingTaskOpt = followingIndex > 0 ? Optional.of(tasks.get(followingIndex - 1)) : Optional.empty();
        }

        Long precedingTaskPosition = precedingTaskOpt.map(Task::getPosition).orElse(0L);
        Long followingTaskPosition = followingTaskOpt.map(Task::getPosition).orElse(precedingTaskPosition + POSITION_GAP * 2);
        Long newTaskPosition = (precedingTaskPosition + followingTaskPosition) / 2;
        newTask.setPosition(newTaskPosition);
        if (followingTaskPosition - precedingTaskPosition <= 1) {
            tasks.add(followingIndex, newTask);
            rebalance(tasks);
        }
        this.tasks = Optional.ofNullable(this.tasks).orElseGet(ArrayList::new);
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
        return Optional.ofNullable(tasks).orElseGet(Collections::emptyList).stream().filter(task -> task.getId().equals(taskId)).findFirst();
    }

    public Optional<Task> taskByIdAndStatus(UUID taskId, TaskStatus status) {
        return taskById(taskId).filter(task -> task.getStatus() == status);
    }

    public Task deleteTaskById(UUID taskId) throws ObjectNotFoundException {
        Task found = Optional.ofNullable(tasks)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(task -> task.getId().equals(taskId)).findFirst()
                .orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        found.delete();
        return found;
    }
}
