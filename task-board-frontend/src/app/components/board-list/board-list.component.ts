import { Component, computed, effect, inject, signal } from '@angular/core';
import { TaskBoardOverviewDto } from '../../models/board.model';
import { TaskBoardService } from '../../services/task-board.service';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-board-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './board-list.component.html',
  styleUrl: './board-list.component.scss',
})
export class BoardListComponent {
  authService = inject(AuthService);
  taskBoardService = inject(TaskBoardService);
  router = inject(Router);

  boards = signal<TaskBoardOverviewDto[]>([]);

  searchPhrase = signal<string>('');

  boardsFiltered = computed<TaskBoardOverviewDto[]>(() => {
    const boards = this.boards();
    const searchPhrase = this.searchPhrase();
    return boards.filter(b => b.name.toLowerCase().includes(searchPhrase.toLowerCase())).sort((a, b) => a.name.localeCompare(b.name));
  });

  newBoardTitle = '';

  taskBoardBeingDeleted = signal<TaskBoardOverviewDto | null>(null);

  loading = signal<boolean>(false);
  boardListEmpty = computed(() => (this.boards()?.length || 0) === 0 && !this.loading());

  private undoTimeoutId: any = null;

  constructor() {
    effect(() => {
      this.authService.isLoggedIn();
    });
  }

  ngOnDestroy() {
    this.executeDelete();
  }

  createBoard() {
    const name = this.newBoardTitle;
    this.taskBoardService.createBoard(name).subscribe({
      next: (t) => {
        this.newBoardTitle = '';
        if (!this.authService.isLoggedIn()) {
          this.router.navigate(['/board', t.id]);
        } else {
          this.executeWithTransition(() => this.boards.update((boards) => [...boards, t]));
        }
      },
      error: (err) => {
        console.error('Błąd tworzenia tablicy', err);
      },
    });
  }

  private executeWithTransition(action: () => void) {
    if (document.startViewTransition) {
      document.startViewTransition(action);
    } else {
      action();
    }
  }

  private dataLoader = effect(() => {
    if (this.authService.isLoggedIn()) this.loadBoards();
    else this.clearBoards();
  });

  private loadBoards() {
    this.loading.set(true);
    this.taskBoardService.findAllBoards().subscribe((data) => {
      this.boards.set(data);
      this.loading.set(false);
    });
  }

  private clearBoards() {
    this.boards.set([]);
  }

  deleteTaskBoard(taskBoard: TaskBoardOverviewDto) {
    if (!taskBoard.id) return;
    if (this.undoTimeoutId) this.executeDelete();
    this.taskBoardBeingDeleted.set(taskBoard);
    this.undoTimeoutId = setTimeout(() => this.executeDelete(), 5000);
    this.executeWithTransition(() =>
      this.boards.update((boards) => boards.filter((tb) => tb.id !== taskBoard.id)),
    );
  }

  private executeDelete() {
    const taskBoard = this.taskBoardBeingDeleted();
    if (taskBoard?.id) {
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;
      this.taskBoardBeingDeleted.set(null);
      this.taskBoardService.deleteBoard(taskBoard.id).subscribe({
        error: (err) => console.error('Error deleting taskboard'),
      });
    }
  }

  onUndoDelete() {
    const taskBoard = this.taskBoardBeingDeleted();
    if (taskBoard?.id) {
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;
      this.executeWithTransition(() => {
        this.boards.update((boards) => [...boards, taskBoard]);
        this.taskBoardBeingDeleted.set(null);
      });
    }
  }
}
