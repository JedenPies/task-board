import { Component, ElementRef, input, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskBoardService } from '../../services/task-board.service';
import { TaskDto, UpdateTaskCommandDto } from '../../models/board.model';

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

  // ZMIANA: Robimy z tego Sygnał
  currentTask = signal<TaskDto | null>(null);
  isSaving = signal<boolean>(false); // to też warto zrobić sygnałem

  constructor() {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(2000)]],
    });
  }

  open(task: TaskDto) {
    // ZMIANA: używamy .set()
    this.currentTask.set(task);

    this.taskForm.patchValue({
      title: task.title,
      description: task.description || '',
    });
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
    if (this.taskForm.invalid || !task) return;

    this.isSaving.set(true);
    const command: UpdateTaskCommandDto = {
      newTitle: this.taskForm.value.title,
      newDescription: this.taskForm.value.description,
    };

    this.taskBoardService.updateTaskDetails(this.boardId(), task.id, command).subscribe({
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
