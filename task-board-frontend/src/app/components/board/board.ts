import { Component, OnInit, input, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { TaskBoard } from '../../services/task-board';
import { TaskDto, TaskStatus } from '../../models/board.model';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, DragDropModule],
  templateUrl: './board.html',
  styleUrl: './board.scss'
})
export class Board implements OnInit {

  id = input.required<string>();
  boardName = signal<string>('Ładowanie...');

  todoTasks = signal<TaskDto[]>([]);
  inProgressTasks = signal<TaskDto[]>([]);
  doneTasks = signal<TaskDto[]>([]);

  recentlyDeletedTask = signal<TaskDto | null>(null);
  private undoTimeoutId: any = null;

  private taskBoardService = inject(TaskBoard);
  private fb = inject(FormBuilder);

  private eventSource: EventSource | null = null;

  taskForm = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['']
  });

  private getSignalByStatus(status: TaskStatus) {
    if (status === 'TODO') return this.todoTasks;
    if (status === 'IN_PROGRESS') return this.inProgressTasks;
    return this.doneTasks;
  }

  ngOnInit(): void {
    this.loadBoardData();
    this.eventSource = new EventSource(`http://localhost:8080/api/task-boards/${this.id()}/sse-stream`)
    this.eventSource.addEventListener("REFRESH", (event) => {
      console.log("Refresh odebrane");
      this.loadBoardData();
    });
    this.eventSource.onerror = (error) => {
      console.error("Błąd połączenia SSE:", error);
    }
  }

  ngOnDestroy(): void {
    if (this.eventSource) {
      this.eventSource.close();
      console.log("Połączenie SSE zamknięte;")
    }
  }

  private loadBoardData() {
    this.taskBoardService.getBoard(this.id()).subscribe({
      next: (boardData) => {
        this.boardName.set(boardData.name ?? 'Tablica Kanban');
        const tasks = boardData.tasks ?? [];

        this.todoTasks.set(tasks.filter(t => t.status === 'TODO'));
        this.inProgressTasks.set(tasks.filter(t => t.status === 'IN_PROGRESS'));
        this.doneTasks.set(tasks.filter(t => t.status === 'DONE'));
      },
      error: (err) => console.error(err)
    });
  }

  onTaskDrop(event: CdkDragDrop<TaskDto[]>, targetStatus: TaskStatus): void {
    // Pobieramy status źródłowy z atrybutu HTML
    const sourceStatus = event.previousContainer.element.nativeElement.getAttribute('data-status') as TaskStatus;

    const sourceSignal = this.getSignalByStatus(sourceStatus);
    const targetSignal = this.getSignalByStatus(targetStatus);

    const sourceArray = [...sourceSignal()];
    const targetArray = [...targetSignal()];

    const backupSource = [...sourceSignal()];
    const backupTarget = [...targetSignal()];

    if (event.previousContainer === event.container) {
      moveItemInArray(sourceArray, event.previousIndex, event.currentIndex);
      sourceSignal.set(sourceArray);
    } else {
      const task = sourceArray[event.previousIndex];
      if (task) {
        task.status = targetStatus;
      }

      transferArrayItem(sourceArray, targetArray, event.previousIndex, event.currentIndex);
      sourceSignal.set(sourceArray);
      targetSignal.set(targetArray);

      if (task && task.id) {
        this.taskBoardService.changeTaskStatus(this.id(), task.id, targetStatus).subscribe({
          error: (err) => {
            console.error('Błąd sieci, przywracam kolejność', err);
            sourceSignal.set(backupSource);
            targetSignal.set(backupTarget);
          }
        });
      }
    }
  }

  onAddTask(): void {
    if (this.taskForm.invalid) return;
    const newTask = this.taskForm.value as TaskDto;
    newTask.id = crypto.randomUUID();
    newTask.status = 'TODO';

    this.taskBoardService.addNewTask(this.id(), newTask).subscribe({
      next: (createdTask) => {
        this.todoTasks.set([...this.todoTasks(), createdTask]);
        this.taskForm.reset();
      },
      error: (err) => console.error(err)
    });
  }

  deleteTaskWithUndo(task: TaskDto): void {
    if (!task.id || !task.status) return;
    if (this.undoTimeoutId) this.executePermanentDelete();

    this.recentlyDeletedTask.set(task);

    const targetSignal = this.getSignalByStatus(task.status);
    targetSignal.set(targetSignal().filter(t => t.id !== task.id));

    this.undoTimeoutId = setTimeout(() => this.executePermanentDelete(), 5000);
  }

  onUndoDelete(): void {
    const task = this.recentlyDeletedTask();
    if (task && task.status) {
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;

      const targetSignal = this.getSignalByStatus(task.status);
      targetSignal.set([...targetSignal(), task]);
      this.recentlyDeletedTask.set(null);
    }
  }

  private executePermanentDelete(): void {
    const task = this.recentlyDeletedTask();
    if (task && task.id) {
      clearTimeout(this.undoTimeoutId);
      this.undoTimeoutId = null;
      this.recentlyDeletedTask.set(null);

      this.taskBoardService.deleteTask(this.id(), task.id).subscribe({
        error: (err) => console.error(err)
      });
    }
  }
}
