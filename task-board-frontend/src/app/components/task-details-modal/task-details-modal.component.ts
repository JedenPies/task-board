import { Component, ElementRef, input, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskBoardService } from '../../services/task-board.service';
import { NewTaskDto, TaskDto, TaskStatus, UpdateTaskCommandDto } from '../../models/board.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-task-details-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-details-modal.component.html',
  styleUrl: './task-details-modal.component.scss',
})
export class TaskDetailsModalComponent {
  boardId = input.required<string>();

  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;

  private fb = inject(FormBuilder);
  private taskBoardService = inject(TaskBoardService);

  taskForm: FormGroup;

  currentTask = signal<TaskDto | null>(null);
  targetStatus = signal<TaskStatus | null>(null);
  isSaving = signal<boolean>(false); // to też warto zrobić sygnałem

  constructor() {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(2000)]],
    });
  }

  openForEdit(task: TaskDto) {
    this.currentTask.set(task);
    this.targetStatus.set(null);
    this.taskForm.patchValue({
      title: task.title,
      description: task.description || '',
    });
    this.dialog.nativeElement.showModal();
  }

  openForCreate(status: TaskStatus) {
    this.currentTask.set(null);
    this.targetStatus.set(status);
    this.taskForm.reset();
    this.dialog.nativeElement.showModal();
  }

  close() {
    this.dialog.nativeElement.close();
    // ZMIANA: czyścimy sygnał
    this.currentTask.set(null);
    this.taskForm.reset();
  }

  save() {
    const task = this.currentTask();
    const status = this.targetStatus();
    if (this.taskForm.invalid || (!task && !status)) return;
    console.log('save: ' + status + ' ' + task);
    let operation$: Observable<any>;
    this.isSaving.set(true);
    if (task) {
      const command: UpdateTaskCommandDto = {
        newTitle: this.taskForm.value.title,
        newDescription: this.taskForm.value.description,
      };
      operation$ = this.taskBoardService.updateTaskDetails(this.boardId(), task.id, command);
    } else if (status !== null && status !== undefined) {
      const newTask: NewTaskDto = {
        title: this.taskForm.value.title,
        description: this.taskForm.value.description,
        status: status,
      };
      operation$ = this.taskBoardService.addNewTask(this.boardId(), newTask);
    } else {
      return;
    }


    operation$.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.close();
      },
      error: (err) => {
        console.error('Błąd zapisu zadania:', err);
        this.isSaving.set(false);
      },
    });
  }
}
