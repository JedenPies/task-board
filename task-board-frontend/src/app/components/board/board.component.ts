import { Component, effect, ElementRef, inject, input, OnDestroy, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { TaskBoardService } from '../../services/task-board.service';
import { TaskBoardDto, TaskDto, TaskStatus } from '../../models/board.model';
import { AuthService } from '../../services/auth.service';
import { PublicFlagModalComponent } from '../go-public-modal/public-flag-modal.component';
import { TaskDetailsModalComponent } from '../task-details-modal/task-details-modal.component';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    DragDropModule,
    PublicFlagModalComponent,
    TaskDetailsModalComponent,
  ],
  templateUrl: './board.component.html',
  styleUrl: './board.component.scss',
})
export class BoardComponent implements OnInit, OnDestroy {
  @ViewChild(PublicFlagModalComponent) goPublicModal!: PublicFlagModalComponent;
  @ViewChild('boardNameInput') boardNameInput?: ElementRef<HTMLInputElement>;

  id = input.required<string>();
  boardName = signal<string>('Ładowanie...');
  board = signal<TaskBoardDto | null>(null);

  todoTasks = signal<TaskDto[]>([]);
  inProgressTasks = signal<TaskDto[]>([]);
  doneTasks = signal<TaskDto[]>([]);

  recentlyDeletedTask = signal<TaskDto | null>(null);
  isEditingName = signal<boolean>(false);
  accessDenied = signal<boolean>(false);

  authService = inject(AuthService);
  private taskBoardService = inject(TaskBoardService);
  private fb = inject(FormBuilder);

  private undoTimeoutId: any = null;
  private eventSource: EventSource | null = null;

  taskForm = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: [''],
  });

  constructor() {
    effect(() => {
      const loggedIn = this.authService.isLoggedIn();
      if (loggedIn) {
        this.onUserLogin();
      } else {
        this.onUserLogout();
      }
    });
  }

  ngOnInit(): void {
    this.loadBoardData();
    this.eventSource = new EventSource(`/api/task-boards/${this.id()}/sse-stream`);

    this.eventSource.addEventListener('REFRESH', () => this.loadBoardData());
    this.eventSource.onerror = (error) => console.error('Błąd połączenia SSE:', error);
  }

  ngOnDestroy(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }
    this.executePermanentDelete();
  }

  private onUserLogin() {
    this.accessDenied.set(false);
    this.loadBoardData();
  }

  private onUserLogout() {
    this.board.set(null);
    this.todoTasks.set([]);
    this.inProgressTasks.set([]);
    this.doneTasks.set([]);
    this.executePermanentDelete();
    this.loadBoardData();
  }

  openVisibilityModal() {
    if (this.board()?.canChangeVisibility) {
      this.goPublicModal?.open();
    }
  }

  enableNameEdit(enable: boolean) {
    if (!enable || this.accessDenied() || !this.board()?.canEdit) {
      this.isEditingName.set(false);
      return;
    }
    this.isEditingName.set(true);
    setTimeout(() => {
      const input = this.boardNameInput?.nativeElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  loadBoardData(): void {
    this.taskBoardService.getBoard(this.id()).subscribe({
      next: (boardData) => {
        this.accessDenied.set(false);
        this.boardName.set(boardData.name ?? 'Tablica Kanban');
        this.board.set(boardData);
        const tasks = boardData.tasks ?? [];
        const deletedTaskId = this.recentlyDeletedTask()?.id;
        this.todoTasks.set(this.sorted(tasks.filter((t) => t.status === 'TODO' && t.id != deletedTaskId)));
        this.inProgressTasks.set(this.sorted(tasks.filter((t) => t.status === 'IN_PROGRESS' && t.id != deletedTaskId)));
        this.doneTasks.set(this.sorted(tasks.filter((t) => t.status === 'DONE' && t.id != deletedTaskId)));
      },
      error: (err) => {
        console.error(err);
        this.board.set(null);
        if (err.status === 403) {
          this.accessDenied.set(true);
          this.boardName.set('Brak dostępu');
        } else {
          this.boardName.set('Błąd ładowania tablicy');
        }
      },
    });
  }

  onUpdateBoardName(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const newName = inputElement.value.trim();
    const oldName = this.boardName();

    this.isEditingName.set(false);
    if (!newName || newName === oldName) return;

    this.boardName.set(newName);

    this.taskBoardService.updateBoardName(this.id(), newName).subscribe({
      error: (err) => {
        console.error('Błąd zapisu nazwy tablicy: ', err);
        this.boardName.set(oldName);
      },
    });
  }

  onTaskDrop(event: CdkDragDrop<TaskDto[]>, targetStatus: TaskStatus): void {
    const sourceStatus = event.previousContainer.id as TaskStatus;

    // 1. Optymistyczna aktualizacja UI
    if (event.previousContainer === event.container) {
      const arr = [...event.container.data];
      moveItemInArray(arr, event.previousIndex, event.currentIndex);
      this.getSignalByStatus(targetStatus).set(arr);
    } else {
      const sourceArr = [...event.previousContainer.data];
      const targetArr = [...event.container.data];

      transferArrayItem(sourceArr, targetArr, event.previousIndex, event.currentIndex);

      this.getSignalByStatus(sourceStatus).set(sourceArr);
      this.getSignalByStatus(targetStatus).set(targetArr);
    }

    // 2. Wyznaczenie followingTaskId
    const targetTasks = this.getSignalByStatus(targetStatus)();
    const movedTask = targetTasks[event.currentIndex];

    // Znajdź zadanie, które będzie bezpośrednio po naszym (jeśli istnieje)
    const nextTask = targetTasks[event.currentIndex + 1];
    const followingTaskId = nextTask ? nextTask.id : null;

    // 3. Wysłanie requestu
    if (movedTask?.id) {
      this.taskBoardService
        .updateTaskPosition(this.id(), movedTask.id, targetStatus, followingTaskId)
        .subscribe({
          error: (err) => {
            console.error('Błąd zmiany pozycji:', err);
            this.loadBoardData(); // Rollback przy błędzie
          },
        });
    }
  }

  deleteTaskWithUndo(task: TaskDto): void {
    if (!task.id || !task.status) return;
    if (this.undoTimeoutId) this.executePermanentDelete();

    this.recentlyDeletedTask.set(task);

    const targetSignal = this.getSignalByStatus(task.status);
    targetSignal.update((tasks) => tasks.filter((t) => t.id !== task.id));

    this.undoTimeoutId = setTimeout(() => this.executePermanentDelete(), 5000);
  }

  onUndoDelete(): void {
    const task = this.recentlyDeletedTask();
    if (task?.status) {
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;

      this.getSignalByStatus(task.status).update((tasks) => this.sorted([...tasks, task]));
      this.recentlyDeletedTask.set(null);
    }
  }

  private executePermanentDelete(): void {
    const task = this.recentlyDeletedTask();
    if (task?.id) {
      this.recentlyDeletedTask.set(null);
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;
      this.taskBoardService.deleteTask(this.id(), task.id).subscribe({
        error: (err) => console.error(err),
      });
    }
  }

  private getSignalByStatus(status: TaskStatus) {
    if (status === 'TODO') return this.todoTasks;
    if (status === 'IN_PROGRESS') return this.inProgressTasks;
    return this.doneTasks;
  }

  private sorted(tasks: TaskDto[]): TaskDto[] {
    return [...tasks].sort((a, b) => a.position - b.position);
  }
}
