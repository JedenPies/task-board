import { Component, ElementRef, inject, input, output, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TaskBoardService } from '../../services/task-board.service';
import { TaskBoardDto } from '../../models/board.model';

@Component({
  selector: 'app-go-public-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './public-flag-modal.component.html',
  styleUrls: ['./public-flag-modal.component.scss'],
})
export class PublicFlagModalComponent {

  @ViewChild('modalElement') modalElement!: ElementRef<HTMLDialogElement>;

  board = input<TaskBoardDto | null>(null);

  visibilityChanged = output<void>();

  isLoading = signal<boolean>(false);

  private taskBoardService = inject(TaskBoardService);

  open() {

    this.modalElement.nativeElement.showModal();
  }
  close() {
    this.modalElement.nativeElement.close();
  }

  toggleVisibility() {
    const currentBoard = this.board();
    if (!currentBoard) return;
    this.isLoading.set(true);
    this.taskBoardService.updateBoardVisibility(this.board()!.id, !this.board()!.isPublic).subscribe({
      next: (board) => {
        this.isLoading.set(false);
        this.close();
      },
      error: (err) => {
        console.error(err);
        this.isLoading.set(false);
        this.close()
      },
    })

  }
}
