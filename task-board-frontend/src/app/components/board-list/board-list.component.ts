import { Component, effect, inject, signal } from '@angular/core';
import { TaskBoardDto, TaskBoardOverviewDto } from '../../models/board.model';
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

  newBoardTitle = '';

  taskBoardBeingDeleted = signal<TaskBoardOverviewDto | null>(null);

  private undoTimeoutId: any = null;

  constructor() {
    effect(() => {
      this.authService.isLoggedIn();
      this.loadBoards();
    });
  }

  ngOnDestroy() {
    this.executeDelete();
  }

  createBoard() {
    const name = this.newBoardTitle;
    this.taskBoardService.createBoard(name).subscribe({
      next: (t) => {
        this.newBoardTitle = t.name;
        this.router.navigate(['/board', t.id]);
      },
      error: (err) => {
        console.error('Błąd tworzenia tablicy', err);
      },
    });
  }

  private dataLoader = effect(() => {
    if (this.authService.isLoggedIn()) this.loadBoards();
    else this.clearBoards();
  });

  private loadBoards() {
    this.taskBoardService.findAllBoards().subscribe((data) => {
      this.boards.set(data);
    });
  }

  private clearBoards() {
    this.boards.set([]);
  }

  deleteTaskBoard(taskBoard: TaskBoardOverviewDto) {
    if (!taskBoard.id) return;
    if (this.undoTimeoutId) this.executeDelete();
    this.taskBoardBeingDeleted.set(taskBoard);
    this.boards.update((boards) => boards.filter((tb) => tb.id !== taskBoard.id));
    this.undoTimeoutId = setTimeout(() => this.executeDelete(), 5000);
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
      this.taskBoardBeingDeleted.set(null);
      this.boards.update((boards) => [...boards, taskBoard]);
    }
  }
}
