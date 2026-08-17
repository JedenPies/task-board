package net.patrykdobrowolski.task_board.service;

import net.patrykdobrowolski.task_board.domain.TaskBoard;
import net.patrykdobrowolski.task_board.domain.UserContext;
import net.patrykdobrowolski.task_board.domain.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TaskBoardAccessEvaluatorTest {

    @Mock
    private UserContext userContext;

    @InjectMocks
    private TaskBoardAccessEvaluator evaluator;

    private final UUID currentUserId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(userContext.getUserId()).thenReturn(currentUserId);
    }

    private TaskBoard createBoard(UUID owner, boolean isPublic) {
        return TaskBoard.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .isPublic(isPublic)
                .build();
    }

    @Nested
    @DisplayName("Edit permissions")
    class EditPermissions {

        @Test
        @DisplayName("Allows to edit a anonymous board.")
        void shouldAllowEditWhenOwnerIsNull() {
            TaskBoard board = createBoard(null, false);

            assertThat(evaluator.isAllowedToEdit(board)).isTrue();
            assertThatCode(() -> evaluator.checkEditPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Allows owner to edit board")
        void shouldAllowEditWhenUserIsOwner() {
            TaskBoard board = createBoard(currentUserId, false);

            assertThat(evaluator.isAllowedToEdit(board)).isTrue();
            assertThatCode(() -> evaluator.checkEditPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Disallow no-owner to edit board")
        void shouldDenyEditWhenUserIsNotOwner() {
            TaskBoard board = createBoard(otherUserId, false);

            assertThat(evaluator.isAllowedToEdit(board)).isFalse();
            assertThatThrownBy(() -> evaluator.checkEditPermissions(board))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Visibility permissions")
    class VisibilityPermissions {

        @Test
        @DisplayName("Disallow to change visibility of anonymous board")
        void shouldDenyVisibilityChangeWhenOwnerIsNull() {
            TaskBoard board = createBoard(null, true);

            assertThat(evaluator.isAllowedToChangeVisibility(board)).isFalse();
            assertThatThrownBy(() -> evaluator.checkPublicFlagPermission(board))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Allows owner to change visibility")
        void shouldAllowVisibilityChangeWhenUserIsOwner() {
            TaskBoard board = createBoard(currentUserId, false);

            assertThat(evaluator.isAllowedToChangeVisibility(board)).isTrue();
            assertThatCode(() -> evaluator.checkPublicFlagPermission(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Disallow non-owners to change board visibility")
        void shouldDenyVisibilityChangeWhenUserIsNotOwner() {
            TaskBoard board = createBoard(otherUserId, true);

            assertThat(evaluator.isAllowedToChangeVisibility(board)).isFalse();
            assertThatThrownBy(() -> evaluator.checkPublicFlagPermission(board))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("View board permissions")
    class ViewPermissions {

        @Test
        @DisplayName("Allows non-owner to view public board")
        void shouldAllowViewWhenBoardIsPublic() {
            TaskBoard board = createBoard(otherUserId, true);

            assertThat(evaluator.isAllowedToView(board)).isTrue();
            assertThatCode(() -> evaluator.checkViewPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Allows owner to view private board")
        void shouldAllowViewWhenPrivateButUserIsOwner() {
            TaskBoard board = createBoard(currentUserId, false);

            assertThat(evaluator.isAllowedToView(board)).isTrue();
            assertThatCode(() -> evaluator.checkViewPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Disallow non-owner to view private board")
        void shouldDenyViewWhenPrivateAndUserIsNotOwner() {
            TaskBoard board = createBoard(otherUserId, false);

            assertThat(evaluator.isAllowedToView(board)).isFalse();
            assertThatThrownBy(() -> evaluator.checkViewPermissions(board))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Tasks manipulation permissions")
    class TaskManipulationPermissions {

        @Test
        @DisplayName("Allows non-owner to edit tasks on public board")
        void shouldAllowTaskManipulationWhenBoardIsPublic() {
            TaskBoard board = createBoard(otherUserId, true);

            assertThat(evaluator.isAllowedToMoveTask(board)).isTrue();
            assertThatCode(() -> evaluator.checkManipulateTasksPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Allows owner to edit task on their private board")
        void shouldAllowTaskManipulationWhenPrivateButUserCanEdit() {
            TaskBoard board = createBoard(currentUserId, false);

            assertThat(evaluator.isAllowedToMoveTask(board)).isTrue();
            assertThatCode(() -> evaluator.checkManipulateTasksPermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Disallow non-owner to edit tasks on private board")
        void shouldDenyTaskManipulationWhenPrivateAndUserCannotEdit() {
            TaskBoard board = createBoard(otherUserId, false);

            assertThat(evaluator.isAllowedToMoveTask(board)).isFalse();
            assertThatThrownBy(() -> evaluator.checkManipulateTasksPermissions(board))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Delete table permissions")
    class DeletePermissions {

        @Test
        @DisplayName("Disallow to remove anonymous board")
        void shouldAllowDeleteWhenOwnerIsNull() {
            TaskBoard board = createBoard(null, false);

            assertThatThrownBy(() -> evaluator.checkDeletePermissions(board))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Allow owner to delete their board")
        void shouldAllowDeleteWhenUserIsOwner() {
            TaskBoard board = createBoard(currentUserId, false);

            assertThatCode(() -> evaluator.checkDeletePermissions(board)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Disallow deletion by non-owners")
        void shouldDenyDeleteWhenUserIsNotOwner() {
            TaskBoard board = createBoard(otherUserId, false);

            assertThatThrownBy(() -> evaluator.checkDeletePermissions(board))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
