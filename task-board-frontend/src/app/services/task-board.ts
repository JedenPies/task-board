import {inject, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TaskBoardDto, TaskDto, TaskStatus} from '../models/board.model';

@Service()
export class TaskBoard {

  private apiUrl = 'http://localhost:8080/api/task-boards'
  private http = inject(HttpClient)

  constructor() {}

  getBoards(): Observable<TaskBoardDto[]> {
    return this.http.get<TaskBoardDto[]>(this.apiUrl);
  }

  getBoard(boardId: string): Observable<TaskBoardDto> {
    return this.http.get<TaskBoardDto>(this.apiUrl + '/' + boardId);
  }

  addNewTask(boardId: string, taskDto: TaskDto): Observable<TaskDto> {
    return this.http.post<TaskDto>(this.apiUrl + '/' + boardId + '/tasks', taskDto);
  }

  changeTaskStatus(boardId: string, taskId: string, newStatus: TaskStatus): Observable<TaskDto> {
    return this.http.put<TaskDto>(this.apiUrl + '/' + boardId + '/tasks/' + taskId + '/status', `"${newStatus}"`,
      { headers: { 'Content-Type': 'application/json' }});
  }

  deleteTask(boardId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + boardId + '/tasks/' + taskId);
  }
}
