import { Component, ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-go-public-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './go-public-modal.html',
  styleUrls: ['./go-public-modal.scss'],
})
export class GoPublicModal {
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;

  open() {
    this.dialog.nativeElement.showModal();
  }
  close() {
    this.dialog.nativeElement.close();
  }
}
