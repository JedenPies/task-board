package net.patrykdobrowolski.task_board.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void shouldUpdateTaskDetails() {
        // Given
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Stary tytuł")
                .description("Stary opis")
                .status(TaskStatus.TODO)
                .position(100L)
                .deleted(false)
                .build();

        UpdateTaskCommand command = new UpdateTaskCommand("Nowy tytuł", "Nowy opis");

        // When
        task.update(command);

        // Then
        assertThat(task.getTitle()).isEqualTo("Nowy tytuł");
        assertThat(task.getDescription()).isEqualTo("Nowy opis");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getPosition()).isEqualTo(100L);
    }

    @Test
    void shouldMarkTaskAsDeleted() {
        // Given
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Zadanie do usunięcia")
                .deleted(false)
                .build();

        // When
        task.delete();

        // Then
        assertThat(task.isDeleted()).isTrue();
    }

    @Test
    void shouldAllowPackageAccessToSetStatusAndPosition() {
        // Given
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Zadanie testowe")
                .status(TaskStatus.TODO)
                .position(100L)
                .build();

        // When
        task.setStatus(TaskStatus.DONE);
        task.setPosition(200L);

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(task.getPosition()).isEqualTo(200L);
    }
}
