package net.patrykdobrowolski.task_board.domain;

import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskBoardTest {

    public static final UUID ID1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ID2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ID3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private TaskBoard taskBoard;
    private final UUID boardId = UUID.randomUUID();
    private final long GAP = 65536L;

    @BeforeEach
    void setUp() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(createTask("T1", GAP, ID1));
        tasks.add(createTask("T2", GAP * 2, ID2));
        tasks.add(createTask("T3", GAP * 3, ID3));

        taskBoard = TaskBoard.builder()
                .id(boardId)
                .tasks(tasks)
                .build();
    }

    @Test
    @DisplayName("Powinien poprawnie przesunąć zadanie pomiędzy dwa inne zadania")
    void shouldMoveTaskBetweenTwoOthers() throws ObjectNotFoundException, CannotMoveTaskException {
        // given
        UUID taskId = taskBoard.taskById(ID1).orElseThrow().getId();
        UUID followingId = taskBoard.taskById(ID3).orElseThrow().getId();

        // when
        taskBoard.moveTask(taskId, TaskStatus.TODO, followingId);

        // then
        Task movedTask = taskBoard.taskById(taskId).orElseThrow();
        assertThat(movedTask.getPosition()).isEqualTo(163840L);
    }

    @Test
    @DisplayName("Powinien uruchomić rebalansowanie, gdy zabraknie miejsca (kolizja)")
    void shouldTriggerRebalanceWhenNoGap() throws ObjectNotFoundException, CannotMoveTaskException {
        // given
        Task t2 = taskBoard.taskById(ID2).orElseThrow();
        Task t3 = taskBoard.taskById(ID3).orElseThrow();
        t2.setPosition(131072L);
        t3.setPosition(131073L);

        // when
        taskBoard.moveTask(ID1, TaskStatus.TODO, t3.getId());

        // then
        assertThat(taskBoard.getTasks())
                .extracting(Task::getPosition)
                .containsExactlyInAnyOrder(GAP, GAP * 2, GAP * 3);
    }

    private Task createTask(String title, long position, UUID id) {
        return Task.builder()
                .id(id)
                .title(title)
                .status(TaskStatus.TODO)
                .position(position)
                .build();
    }
}
