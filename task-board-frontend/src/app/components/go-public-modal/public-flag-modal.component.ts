import { Component, ElementRef, inject, input, output, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BoardComponent } from '../board/board.component';
import { TaskBoardService } from '../../services/task-board.service';

@Component({
  selector: 'app-go-public-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './public-flag-modal.component.html',
  styleUrls: ['./public-flag-modal.component.scss'],
})
export class PublicFlagModalComponent {

  @ViewChild('modalElement') modalElement!: ElementRef<HTMLDialogElement>;

  board = input<BoardComponent | null>(null);

  visibilityChanged = output<void>();

  private taskBoardService = inject(TaskBoardService);
  isLoading = signal<boolean>(false);

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


  }
}
