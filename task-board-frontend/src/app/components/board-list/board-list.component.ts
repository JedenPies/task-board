import { Component, effect, inject, signal, ViewChild } from '@angular/core';
import { TaskBoardDto } from '../../models/board.model';
import { TaskBoardService } from '../../services/task-board.service';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { LoginModalComponent } from '../login-modal/login-modal.component';

@Component({
  selector: 'app-board-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoginModalComponent],
  templateUrl: './board-list.component.html',
  styleUrl: './board-list.component.scss',
})
export class BoardListComponent {
  authService = inject(AuthService);
  taskBoardService = inject(TaskBoardService);
  router = inject(Router);

  boards = signal<TaskBoardDto[]>([]);

  newBoardTitle = '';

  createBoard() {
    const name = this.newBoardTitle;
    this.taskBoardService.createBoard(name).subscribe({
      next: (t ) => {
        this.newBoardTitle = t.name;
        this.router.navigate(['/board', t.id ]);
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
}
