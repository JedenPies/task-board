package net.patrykdobrowolski.task_board.domain;

import net.patrykdobrowolski.task_board.domain.exception.CannotMoveTaskException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskBoardTest {

    public static final UUID ID1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ID2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ID3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID ID4 = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private TaskBoard taskBoard;
    private final UUID boardId = UUID.randomUUID();
    private final long GAP = 65536L;

    @BeforeEach
    void setUp() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(createTask(ID1, TaskStatus.TODO, GAP));
        tasks.add(createTask(ID2, TaskStatus.TODO, GAP * 2));
        tasks.add(createTask(ID3, TaskStatus.TODO, GAP * 3));
        tasks.add(createTask(ID4, TaskStatus.DONE, GAP));

        taskBoard = TaskBoard.builder()
                .id(boardId)
                .name("Test Board")
                .tasks(tasks)
                .build();
    }

    // ==========================================
    // TESTY DODAWANIA ZADAŃ
    // ==========================================

    @Test
    @DisplayName("Powinien dodać nowe zadanie na początek istniejącej listy")
    void shouldAddNewTaskAtTheBeginning() throws Exception {
        UUID newTaskId = UUID.randomUUID();
        Task newTask = Task.builder().id(newTaskId).title("New Task").build();

        taskBoard.addNewTask(newTask);

        assertThat(taskBoard.getTasks()).hasSize(5);
        assertThat(newTask.getPosition()).isEqualTo(GAP / 2);
    }

    @Test
    @DisplayName("Powinien dodać pierwsze zadanie z pozycją GAP na pustej tablicy")
    void shouldAddNewTaskToEmptyBoard() throws Exception {
        TaskBoard emptyBoard = TaskBoard.builder().id(UUID.randomUUID()).name("Pusta Tablica").build();
        Task newTask = Task.builder().id(UUID.randomUUID()).title("Pierwsze zadanie").build();

        emptyBoard.addNewTask(newTask);

        assertThat(emptyBoard.getTasks()).hasSize(1);
        assertThat(newTask.getPosition()).isEqualTo(GAP);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy próbie dodania zadania o istniejącym ID")
    void shouldThrowExceptionWhenTaskWithSameIdAlreadyExists() {
        Task duplicateTask = Task.builder().id(ID1).title("Duplikat").build();

        assertThatThrownBy(() -> taskBoard.addNewTask(duplicateTask))
                .isInstanceOf(ObjectAlreadyExistsException.class);
    }

    // ==========================================
    // TESTY PRZESUWANIA ZADAŃ
    // ==========================================

    @Test
    @DisplayName("Powinien przesunąć zadanie na początek kolumny")
    void shouldMoveTaskToBeginningOfColumn() throws ObjectNotFoundException, CannotMoveTaskException {
        UUID taskId = taskBoard.taskById(ID3).orElseThrow().getId();
        UUID followingTaskId = taskBoard.taskById(ID1).orElseThrow().getId();

        taskBoard.moveTask(taskId, TaskStatus.TODO, followingTaskId);

        Task movedTask = taskBoard.taskById(taskId).orElseThrow();
        assertThat(movedTask.getPosition()).isEqualTo(GAP / 2);
    }

    @Test
    @DisplayName("Powinien przesunąć zadanie do innej kolumny (zmiana statusu)")
    void shouldMoveTaskToAnotherColumn() throws ObjectNotFoundException, CannotMoveTaskException {
        UUID taskId = taskBoard.taskById(ID1).orElseThrow().getId();

        taskBoard.moveTask(taskId, TaskStatus.IN_PROGRESS, null);

        Task movedTask = taskBoard.taskById(taskId).orElseThrow();
        assertThat(movedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(movedTask.getPosition()).isEqualTo(GAP);
    }

    @Test
    @DisplayName("Powinien poprawnie przesunąć zadanie pomiędzy dwa inne zadania")
    void shouldMoveTaskBetweenTwoTasks() throws ObjectNotFoundException, CannotMoveTaskException {
        UUID taskId = taskBoard.taskById(ID3).orElseThrow().getId();
        UUID followingTaskId = taskBoard.taskById(ID2).orElseThrow().getId();

        taskBoard.moveTask(taskId, TaskStatus.TODO, followingTaskId);

        Task movedTask = taskBoard.taskById(taskId).orElseThrow();
        assertThat(movedTask.getPosition()).isEqualTo(GAP + GAP / 2);
    }

    @Test
    @DisplayName("Powinien poprawnie przesunąć zadanie na koniec kolumny (brak followingTask)")
    void shouldMoveTaskToTheEndWhenNoFollowingTaskIsProvided() throws ObjectNotFoundException, CannotMoveTaskException {
        UUID taskId = taskBoard.taskById(ID1).orElseThrow().getId();

        taskBoard.moveTask(taskId, TaskStatus.TODO, null);

        Task movedTask = taskBoard.taskById(taskId).orElseThrow();
        assertThat(movedTask.getPosition()).isEqualTo(GAP * 4);
    }

    @Test
    @DisplayName("Powinien uruchomić rebalansowanie, gdy zabraknie miejsca (kolizja)")
    void shouldTriggerRebalanceWhenNoGap() throws ObjectNotFoundException, CannotMoveTaskException {
        Task t2 = taskBoard.taskById(ID2).orElseThrow();
        Task t3 = taskBoard.taskById(ID3).orElseThrow();
        t2.setPosition(131072L);
        t3.setPosition(131073L);

        taskBoard.moveTask(ID1, TaskStatus.TODO, t3.getId());

        assertThat(taskBoard.getTasks()).hasSize(4);
        assertThat(taskBoard.getTasks().stream().filter(t -> t.getStatus() == TaskStatus.TODO))
                .extracting(Task::getPosition)
                .containsExactlyInAnyOrder(GAP, GAP * 2, GAP * 3);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy próbie przesunięcia nieistniejącego zadania")
    void shouldThrowExceptionWhenMovingNonExistingTask() {
        UUID nonExistingTaskId = UUID.randomUUID();

        assertThatThrownBy(() -> taskBoard.moveTask(nonExistingTaskId, TaskStatus.TODO, null))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy braku zadania następującego w danej kolumnie")
    void shouldThrowExceptionWhenFollowingTaskDoesNotExistInColumn() {
        UUID nonExistingFollowingId = UUID.randomUUID();

        assertThatThrownBy(() -> taskBoard.moveTask(ID1, TaskStatus.TODO, nonExistingFollowingId))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy próbie przesunięcia zadania na samo siebie")
    void shouldThrowExceptionWhenMovingTaskOntoItself() {
        assertThatThrownBy(() -> taskBoard.moveTask(ID1, TaskStatus.TODO, ID1))
                .isInstanceOf(CannotMoveTaskException.class);
    }

    // ==========================================
    // TESTY EDYCJI I USUWANIA ZADAŃ
    // ==========================================

    @Test
    @DisplayName("Powinien poprawnie edytować dane zadania")
    void shouldEditExistingTask() throws ObjectNotFoundException {
        UpdateTaskCommand command = new UpdateTaskCommand("Nowy Tytuł", "Nowy Opis");

        Task editedTask = taskBoard.editTask(ID1, command);

        assertThat(editedTask.getTitle()).isEqualTo("Nowy Tytuł");
        assertThat(editedTask.getDescription()).isEqualTo("Nowy Opis");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy próbie edycji nieistniejącego zadania")
    void shouldThrowExceptionWhenEditingNonExistingTask() {
        UpdateTaskCommand command = new UpdateTaskCommand("Tytuł", "Opis");

        assertThatThrownBy(() -> taskBoard.editTask(UUID.randomUUID(), command))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Powinien oznaczyć zadanie jako usunięte")
    void shouldMarkTaskAsDeleted() throws ObjectNotFoundException {
        Task deletedTask = taskBoard.deleteTaskById(ID1);

        assertThat(deletedTask.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy próbie usunięcia nieistniejącego zadania")
    void shouldThrowExceptionWhenDeletingNonExistingTask() {
        assertThatThrownBy(() -> taskBoard.deleteTaskById(UUID.randomUUID()))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ==========================================
    // TESTY STANU TABLICY
    // ==========================================

    @Test
    @DisplayName("Powinien zmienić nazwę tablicy")
    void shouldChangeBoardName() {
        taskBoard.changeName("Nowa Nazwa Tablicy");

        assertThat(taskBoard.getName()).isEqualTo("Nowa Nazwa Tablicy");
    }

    @Test
    @DisplayName("Powinien oznaczyć tablicę jako usuniętą")
    void shouldMarkBoardAsDeleted() {
        taskBoard.delete();

        assertThat(taskBoard.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Powinien zwrócić zadanie, gdy ID i status się zgadzają")
    void shouldReturnTaskWhenIdAndStatusMatch() {
        // when
        // TASK_1 w metodzie setUp() został utworzony ze statusem TODO
        Optional<Task> foundTask = taskBoard.taskByIdAndStatus(ID1, TaskStatus.TODO);

        // then
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getId()).isEqualTo(ID1);
        assertThat(foundTask.get().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, gdy zadanie istnieje, ale ma inny status")
    void shouldReturnEmptyOptionalWhenTaskExistsButStatusIsDifferent() {
        // when
        // Szukamy TASK_1 (który jest TODO), podając status IN_PROGRESS
        Optional<Task> foundTask = taskBoard.taskByIdAndStatus(ID1, TaskStatus.IN_PROGRESS);

        // then
        assertThat(foundTask).isEmpty();
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, gdy zadanie o podanym ID w ogóle nie istnieje")
    void shouldReturnEmptyOptionalWhenTaskDoesNotExist() {
        // given
        UUID randomId = UUID.randomUUID();

        // when
        Optional<Task> foundTask = taskBoard.taskByIdAndStatus(randomId, TaskStatus.TODO);

        // then
        assertThat(foundTask).isEmpty();
    }

    private Task createTask(UUID id, TaskStatus status, long position) {
        return Task.builder()
                .id(id)
                .title("Task " + id)
                .status(status)
                .position(position)
                .build();
    }
}
