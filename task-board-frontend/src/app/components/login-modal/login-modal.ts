import { Component, ElementRef, ViewChild, inject, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login-modal.html',
  styleUrls: ['./login-modal.scss'],
})
export class LoginModal {

  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;

  loginSuccess = output<void>();

  username = '';
  password = '';
  authService = inject(AuthService);

  open() {
    this.dialog.nativeElement.showModal();
  }
  close() {
    this.dialog.nativeElement.close();
  }

  onLogin(event: Event) {
    event.preventDefault();
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.username = '';
        this.password = '';
        this.loginSuccess.emit();
        this.close();
      },
      error: () => alert('Błąd logowania'),
    });
  }
}
