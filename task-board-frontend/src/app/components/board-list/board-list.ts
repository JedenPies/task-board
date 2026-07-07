import {Component, OnInit, signal} from '@angular/core';
import {TaskBoardDto} from '../../models/board.model';
import {TaskBoard} from '../../services/task-board';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-board-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './board-list.html',
  styleUrl: './board-list.scss',
})
export class BoardList implements OnInit {

  boards = signal<TaskBoardDto[]>([]);

  constructor(private taskBoardService: TaskBoard) {}

  ngOnInit(): void {
    this.taskBoardService.getBoards().subscribe({
      next: (data) => {
        this.boards.set(data);
      },
      error: (err) => {
        console.error('Coś poszło nie tak z pobieraniem tablic', err)
      }
    })
  }
}
