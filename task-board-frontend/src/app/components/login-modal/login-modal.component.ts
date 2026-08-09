import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { LoaderComponent } from '../loader/loader.component';
import { Router } from '@angular/router';


declare var google: any;

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

  private router = inject(Router);

  private githubClientId = 'Ov23li2tJ3I9sGVFdlva';
  private googleClientId = '414596253914-8cdt79p5tk6biv66pk1h0o21l40gd8uu.apps.googleusercontent.com';

  errorMessage = signal<string | null>(null);

  username = '';
  password = '';

  ngAfterViewInit() {
    this.initializeGoogleSignIn();
  }

  private initializeGoogleSignIn() {
    google.accounts.id.initialize({
      client_id: this.googleClientId,
      callback: (response: any) => this.handleGoogleResponse(response),
    });

    google.accounts.id.renderButton(
      document.getElementById('google-btn'),
      { theme: 'outline', size: 'large', width: 320 },
    );
  }

  private handleGoogleResponse(response: any) {
    this.authService.loginWithGoogle(response.credential).subscribe();
    this.close();
  }

  loginWithGithub() {
    sessionStorage.setItem('postLoginRedirect', this.router.url);
    window.location.href = `https://github.com/login/oauth/authorize?client_id=${this.githubClientId}`;
  }

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
