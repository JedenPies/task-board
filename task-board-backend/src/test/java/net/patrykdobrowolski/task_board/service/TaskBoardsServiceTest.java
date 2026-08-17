package net.patrykdobrowolski.task_board.service;

import net.patrykdobrowolski.task_board.db.TaskBoardsRepositoryService;
import net.patrykdobrowolski.task_board.domain.*;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskBoardsServiceTest {

    @Mock
    private TaskBoardsRepositoryService repositoryService;

    @Mock
    private UserContext userContext;

    @Mock
    private TaskBoardAccessEvaluator taskBoardAccess;

    @InjectMocks
    private TaskBoardsService taskBoardsService;

    private final UUID boardId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Nested
    class FindAllBoardsTest {

        @Test
        void shouldReturnEmptyListWhenUserIsNotLoggedIn() {
            // given
            when(userContext.isLoggedIn()).thenReturn(false);

            // when
            List<TaskBoard> result = taskBoardsService.findAllBoards();

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(repositoryService);
        }

        @Test
        void shouldReturnUserBoardsWhenUserIsLoggedIn() {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(userContext.isLoggedIn()).thenReturn(true);
            when(userContext.getUserId()).thenReturn(userId);
            when(repositoryService.findAllBoards(userId)).thenReturn(List.of(mockBoard));

            // when
            List<TaskBoard> result = taskBoardsService.findAllBoards();

            // then
            assertThat(result).containsExactly(mockBoard);
        }
    }

    @Nested
    class AddTaskToBoardTest {

        @Test
        void shouldAddTaskToBoard() throws Exception {

            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            Task task = Task.builder().id(taskId).build();

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));
            when(mockBoard.addNewTask(task)).thenReturn(task);

            // when
            Task result = taskBoardsService.addTaskToBoard(boardId, task);

            // then
            assertThat(result).isEqualTo(task);
            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(mockBoard).addNewTask(task);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldThrowExceptionWhenAddingTaskToNonExistingBoard() {

            // given
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.addTaskToBoard(boardId, Task.builder().id(taskId).build())).isInstanceOf(ObjectNotFoundException.class).hasMessageContaining("board");

            verifyNoInteractions(taskBoardAccess);
        }
    }


    @Nested
    class MoveTaskTest {

        @Test
        void shouldMoveTask() throws Exception {

            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            UUID followingTaskId = UUID.randomUUID();

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // when
            TaskBoard result = taskBoardsService.moveTask(boardId, taskId, followingTaskId, TaskStatus.IN_PROGRESS);

            // then
            assertThat(result).isEqualTo(mockBoard);
            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(mockBoard).moveTask(taskId, TaskStatus.IN_PROGRESS, followingTaskId);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldDenyMovingTaskWhenNoPermissions() throws Exception {

            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.moveTask(boardId, taskId, null, TaskStatus.DONE))
                    .isInstanceOf(AccessDeniedException.class);

            verify(mockBoard, never()).moveTask(any(), any(), any());
            verify(repositoryService, never()).save(any());
        }
    }

    @Nested
    class DeleteBoardTest {

        @Test
        void shouldDeleteBoard() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // when
            taskBoardsService.deleteBoard(boardId);

            // then
            verify(taskBoardAccess).checkDeletePermissions(mockBoard);
            verify(mockBoard).delete();
            verify(repositoryService).save(mockBoard);
        }
    }

    @Nested
    class CreateBoardTest {

        @Test
        void shouldCreatePublicBoardWithNoOwnerWhenUserIsNotLoggedIn() {
            // given
            TaskBoard inputBoard = TaskBoard.builder()
                    .name("Nowa Tablica")
                    .isPublic(false)
                    .build();

            when(userContext.isLoggedIn()).thenReturn(false);
            when(userContext.getUserId()).thenReturn(null);

            TaskBoard savedBoard = TaskBoard.builder().id(UUID.randomUUID()).build();
            when(repositoryService.save(any(TaskBoard.class))).thenReturn(savedBoard);

            // when
            TaskBoard result = taskBoardsService.createBoard(inputBoard);

            // then
            assertThat(result).isEqualTo(savedBoard);

            ArgumentCaptor<TaskBoard> boardCaptor = ArgumentCaptor.forClass(TaskBoard.class);
            verify(repositoryService).save(boardCaptor.capture());

            TaskBoard capturedBoard = boardCaptor.getValue();

            assertThat(capturedBoard.getIsPublic()).isTrue();
            assertThat(capturedBoard.getOwner()).isNull();
        }

        @Test
        void shouldCreateBoardWithOwnerAndKeepPrivacyWhenUserIsLoggedIn() {
            // given
            UUID currentUserId = UUID.randomUUID();

            TaskBoard inputBoard = TaskBoard.builder()
                    .name("Tajny Projekt")
                    .isPublic(false)
                    .build();

            when(userContext.isLoggedIn()).thenReturn(true);
            when(userContext.getUserId()).thenReturn(currentUserId);

            TaskBoard savedBoard = TaskBoard.builder().id(UUID.randomUUID()).build();
            when(repositoryService.save(any(TaskBoard.class))).thenReturn(savedBoard);

            // when
            TaskBoard result = taskBoardsService.createBoard(inputBoard);

            // then
            assertThat(result).isEqualTo(savedBoard);

            ArgumentCaptor<TaskBoard> boardCaptor = ArgumentCaptor.forClass(TaskBoard.class);
            verify(repositoryService).save(boardCaptor.capture());

            TaskBoard capturedBoard = boardCaptor.getValue();

            assertThat(capturedBoard.getIsPublic()).isFalse();
            assertThat(capturedBoard.getOwner()).isEqualTo(currentUserId);
        }
    }

    @Nested
    class FindBoardTest {

        @Test
        void shouldReturnBoardAndViewPermissionsAreChecked() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // when
            TaskBoard result = taskBoardsService.findBoard(boardId);

            // then
            assertThat(result).isEqualTo(mockBoard);
            // KLUCZOWE: Upewniamy się, że wywołano metodę sprawdzającą uprawnienia!
            verify(taskBoardAccess).checkViewPermissions(mockBoard);
        }

        @Test
        void shouldThrowAccessDeniedExceptionWhenNoViewPermissions() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkViewPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.findBoard(boardId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkViewPermissions(mockBoard);
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenBoardDoesNotExistOnFind() throws Exception {
            // given
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.findBoard(boardId))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkViewPermissions(any());
        }
    }

    @Nested
    class FindTaskTest {
        @Test
        @DisplayName("Powinien zwrócić zadanie i zweryfikować uprawnienia, gdy tablica i zadanie istnieją")
        void shouldReturnTaskWhenBoardAndTaskExist() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            Task mockTask = mock(Task.class);

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));
            when(mockBoard.taskById(taskId)).thenReturn(Optional.of(mockTask));

            // when
            Task result = taskBoardsService.findTask(boardId, taskId);

            // then
            assertThat(result).isEqualTo(mockTask);
            verify(taskBoardAccess).checkViewPermissions(mockBoard);
            verify(mockBoard).taskById(taskId);
        }

        @Test
        void shouldThrowAccessDeniedExceptionWhenNoPermissionsToFindTask() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkViewPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.findTask(boardId, taskId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkViewPermissions(mockBoard);
            verify(mockBoard, never()).taskById(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenBoardDoesNotExistOnFindTask() throws AccessDeniedException {
            // given
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.findTask(boardId, taskId))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkManipulateTasksPermissions(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenTaskDoesNotExistInBoard() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));
            when(mockBoard.taskById(taskId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.findTask(boardId, taskId))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskBoardAccess).checkViewPermissions(mockBoard);
        }
    }

    @Nested
    class DeleteTaskTest {

        @Test
        void shouldDeleteTaskAndSaveBoard() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            Task mockTask = mock(Task.class); // usunięte zadanie

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));
            when(mockBoard.deleteTaskById(taskId)).thenReturn(mockTask);

            // when
            Task result = taskBoardsService.deleteTask(boardId, taskId);

            // then
            assertThat(result).isEqualTo(mockTask);
            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(mockBoard).deleteTaskById(taskId);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldThrowAccessDeniedWhenNoPermissionToDeleteTask() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.deleteTask(boardId, taskId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);

            verify(mockBoard, never()).deleteTaskById(any());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenBoardDoesNotExistOnDeleteTask() throws AccessDeniedException {
            // given
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.deleteTask(boardId, taskId))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkManipulateTasksPermissions(any());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenTaskNotFoundInBoardOnDelete() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            when(mockBoard.deleteTaskById(taskId)).thenThrow(ObjectNotFoundException.of("Task", taskId));

            // when & then
            assertThatThrownBy(() -> taskBoardsService.deleteTask(boardId, taskId))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(repositoryService, never()).save(any());
        }
    }

    @Nested
    class ChangeBoardNameTest {

        @Test
        void shouldChangeBoardNameAndSave() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            String newName = "Nowa super nazwa";

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // when
            TaskBoard result = taskBoardsService.changeName(boardId, newName);

            // then
            assertThat(result).isEqualTo(mockBoard);

            verify(taskBoardAccess).checkEditPermissions(mockBoard);
            verify(mockBoard).changeName(newName);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldThrowAccessDeniedWhenNoPermissionToChangeName() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            String newName = "Zhakowana nazwa";

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkEditPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.changeName(boardId, newName))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkEditPermissions(mockBoard);
            verify(mockBoard, never()).changeName(anyString());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenChangingNameOfNonExistingBoard() throws AccessDeniedException {
            // given
            String newName = "Widmo";
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.changeName(boardId, newName))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkEditPermissions(any());
            verify(repositoryService, never()).save(any());
        }
    }

    @Nested
    class ChangeBoardVisibilityTest {

        @Test
        void shouldChangePublicFlagAndSave() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            Boolean newPublicFlag = true;

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // when
            TaskBoard result = taskBoardsService.setPublicFlag(boardId, newPublicFlag);

            // then
            assertThat(result).isEqualTo(mockBoard);

            verify(taskBoardAccess).checkPublicFlagPermission(mockBoard);
            verify(mockBoard).setIsPublic(newPublicFlag);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldThrowAccessDeniedWhenNoPermissionToChangePublicFlag() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkPublicFlagPermission(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.setPublicFlag(boardId, true))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkPublicFlagPermission(mockBoard);

            verify(mockBoard, never()).setIsPublic(anyBoolean());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenChangingPublicFlagOfNonExistingBoard() throws AccessDeniedException {
            // given
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.setPublicFlag(boardId, true))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkPublicFlagPermission(any());
            verify(repositoryService, never()).save(any());
        }
    }

    @Nested
    class EditTaskTest {
        @Test
        void shouldEditTaskAndSaveBoard() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            Task mockTask = mock(Task.class); // Zadanie po edycji
            UpdateTaskCommand command = new UpdateTaskCommand("Nowy Tytuł", "Nowy Opis");

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));
            when(mockBoard.editTask(taskId, command)).thenReturn(mockTask);

            // when
            Task result = taskBoardsService.editTask(boardId, taskId, command);

            // then
            assertThat(result).isEqualTo(mockTask);

            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(mockBoard).editTask(taskId, command);
            verify(repositoryService).save(mockBoard);
        }

        @Test
        void shouldThrowAccessDeniedWhenNoPermissionToEditTask() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            UpdateTaskCommand command = new UpdateTaskCommand("Tytuł", "Opis");

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            doThrow(new AccessDeniedException())
                    .when(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);

            // when & then
            assertThatThrownBy(() -> taskBoardsService.editTask(boardId, taskId, command))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(mockBoard, never()).editTask(any(), any());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenBoardDoesNotExistOnEditTask() throws AccessDeniedException {
            // given
            UpdateTaskCommand command = new UpdateTaskCommand("Tytuł", "Opis");
            when(repositoryService.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskBoardsService.editTask(boardId, taskId, command))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Board");

            verify(taskBoardAccess, never()).checkManipulateTasksPermissions(any());
            verify(repositoryService, never()).save(any());
        }

        @Test
        void shouldThrowObjectNotFoundExceptionWhenTaskNotFoundInBoardOnEdit() throws Exception {
            // given
            TaskBoard mockBoard = mock(TaskBoard.class);
            UpdateTaskCommand command = new UpdateTaskCommand("Tytuł", "Opis");

            when(repositoryService.findById(boardId)).thenReturn(Optional.of(mockBoard));

            when(mockBoard.editTask(taskId, command)).thenThrow(ObjectNotFoundException.of("Task", taskId));

            // when & then
            assertThatThrownBy(() -> taskBoardsService.editTask(boardId, taskId, command))
                    .isInstanceOf(ObjectNotFoundException.class)
                    .hasMessageContaining("Task");

            verify(taskBoardAccess).checkManipulateTasksPermissions(mockBoard);
            verify(repositoryService, never()).save(any());
        }
    }
}