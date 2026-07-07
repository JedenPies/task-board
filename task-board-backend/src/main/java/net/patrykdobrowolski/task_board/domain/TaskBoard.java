package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;
import lombok.Getter;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
public class TaskBoard {

    @Getter
    private final UUID id;

    @Getter
    private String name;
    private List<Task> tasks;

    public void addTask(Task task) {
       tasks.add(task);
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(Optional.ofNullable(tasks).orElseGet(Collections::emptyList));
    }

    public Optional<Task> taskById(UUID taskId) {
        return tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst();
    }

    public Task deleteTaskById(UUID taskId) throws ObjectNotFoundException {
        Task found = tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst().orElseThrow(() -> ObjectNotFoundException.of("Task", taskId));
        tasks.remove(found);
        return found;
    }

    public void changeName(String newName) {
        this.name = newName;

    }
}
