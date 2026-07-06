package net.patrykdobrowolski.task_board.domain;

import lombok.Builder;
import lombok.Getter;

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
}
