import {inject, Service} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  TaskBoardDto,
  TaskBoardOverviewDto,
  TaskDto,
  TaskStatus,
  UpdateTaskCommandDto,
} from '../models/board.model';

@Service()
export class TaskBoardService {
  private apiUrl = 'http://localhost:8081/api/task-boards';
  private http = inject(HttpClient);

  constructor() {}

  findAllBoards(): Observable<TaskBoardOverviewDto[]> {
    return this.http.get<TaskBoardOverviewDto[]>(this.apiUrl);
  }

  createBoard(name: string): Observable<TaskBoardDto> {
    return this.http.post<TaskBoardDto>(this.apiUrl, { name });
  }

  getBoard(boardId: string): Observable<TaskBoardDto> {
    return this.http.get<TaskBoardDto>(this.apiUrl + '/' + boardId);
  }

  addNewTask(boardId: string, taskDto: TaskDto): Observable<TaskDto> {
    return this.http.post<TaskDto>(this.apiUrl + '/' + boardId + '/tasks', taskDto);
  }

  changeTaskStatus(boardId: string, taskId: string, newStatus: TaskStatus): Observable<TaskDto> {
    return this.http.put<TaskDto>(
      this.apiUrl + '/' + boardId + '/tasks/' + taskId + '/status',
      `"${newStatus}"`,
      { headers: { 'Content-Type': 'application/json' } },
    );
  }

  deleteTask(boardId: string, taskId: string): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + boardId + '/tasks/' + taskId);
  }

  updateBoardName(boardId: string, newName: string): Observable<void> {
    const command = { newName: newName }
    return this.http.put<void>(this.apiUrl + '/' + boardId + '/name', command, {
      headers: { 'Content-Type': 'application/json' },
    });
  }

  updateBoardVisibility(boardId: string, isPublic: boolean): Observable<void> {
    const command = { isPublic: isPublic }
    return this.http.put<void>(this.apiUrl + '/' + boardId + '/visibility', command, {
      headers: { 'Content-Type': 'application/json' },
    });
  }

  updateTaskPosition(boardId: string, taskId: string, status: TaskStatus, followingTaskId: string | null): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/${boardId}/tasks/${taskId}/position`,
      { status, followingTaskId },
      { headers: { 'Content-Type': 'application/json' } },
    );
  }

  updateTaskDetails(boardId: string, taskId: string, command: UpdateTaskCommandDto) {
    return this.http.post<TaskDto>(
      `${this.apiUrl}/${boardId}/tasks/${taskId}/update-task-requests`,
      command
    );
  }
}
