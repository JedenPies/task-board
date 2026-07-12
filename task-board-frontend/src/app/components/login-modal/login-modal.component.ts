import { Component, ElementRef, ViewChild, inject, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.scss'],
})
export class LoginModalComponent {

  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;

  authService = inject(AuthService);

  loginSuccess = output<void>();

  username = '';
  password = '';

  // noinspection JSUnusedGlobalSymbols
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
