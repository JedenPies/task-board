import { Component, inject, OnInit, signal } from '@angular/core';
import { TaskBoardDto } from '../../models/board.model';
import { TaskBoardService } from '../../services/task-board';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-board-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink ],
  templateUrl: './board-list.html',
  styleUrl: './board-list.scss',
})
export class BoardList implements OnInit {

  authService = inject(AuthService);
  taskBoardService = inject(TaskBoardService);
  router = inject(Router);

  boards = signal<TaskBoardDto[]>([]);

  newBoardTitle = '';

  username = '';
  password = '';
  loginError = signal<string | null>(null);

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadBoards();
    }
  }

  loadBoards() {
    this.taskBoardService.findAllBoards().subscribe(data => {
      this.boards.set(data);
    })
  }

  createBoard() {
    const id = crypto.randomUUID();
    const name = this.newBoardTitle;
    this.taskBoardService.createBoard(id, name).subscribe({
      next: () => {
        this.newBoardTitle = '';
        console.log('Tablica utworzona');
        this.router.navigate(['/board', id]);
      },
      error: (err) => {
        console.error('Błąd tworzenia tablicy', err);
      }
    })
  }

  login() {
    this.loginError.set(null);
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.username = '';
        this.password = '';
        this.loadBoards();
      },
      error: (err) => {
        console.error('Błąd logowania', err);
        this.loginError.set(err.message);
      }
    })
  }

  logout() {
    this.authService.logout();
    this.boards.set([]);
  }
}
