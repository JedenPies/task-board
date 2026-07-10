import {inject, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TaskBoardDto, TaskDto, TaskStatus} from '../models/board.model';

@Service()
export class TaskBoardService {

  private apiUrl = 'http://localhost:8081/api/task-boards'
  private http = inject(HttpClient)

  constructor() {}

  findAllBoards(): Observable<TaskBoardDto[]> {
    return this.http.get<TaskBoardDto[]>(this.apiUrl);
  }

  createBoard(id: string, name: string): Observable<TaskBoardDto> {
    return this.http.post<TaskBoardDto>(this.apiUrl, { id, name });
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

  updateBoardName(boardId: string, newName: string): Observable<void> {
    return this.http.put<void>(this.apiUrl + '/' + boardId + '/name', `${newName}`,
      { headers: { 'Content-Type': 'application/json' }});
  }
}
