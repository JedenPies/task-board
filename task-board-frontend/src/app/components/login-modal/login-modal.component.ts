import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { LoaderComponent } from '../loader/loader.component';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [FormsModule, LoaderComponent],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.scss'],
})
export class LoginModalComponent {
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;

  authService = inject(AuthService);

  errorMessage = signal<string | null>(null);

  username = '';
  password = '';

  open() {
    this.errorMessage.set(null);
    this.password = '';
    this.username = '';
    this.dialog.nativeElement.showModal();
  }
  close() {
    this.dialog.nativeElement.close();
  }

  onLogin(event: Event) {
    this.errorMessage.set(null);
    event.preventDefault();
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.close();
        this.password = '';
        this.username = '';
      },
      error: () => {
        this.password = '';
        this.errorMessage.set('Niepoprawne dane logowania lub hasło');
      },
    });
  }
}
