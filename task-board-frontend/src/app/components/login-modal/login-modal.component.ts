import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

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

  private router = inject(Router);

  private githubClientId = environment.githubClientId;
  private googleClientId = environment.googleClientId;
  private facebookClientId = environment.facebookClientId;
  private linkedinClientId = environment.linkedinClientId;

  errorMessage = signal<string | null>(null);

  username = '';
  password = '';

  loginWithGoogle() {
    sessionStorage.setItem('postLoginRedirect', this.router.url);
    const redirectUri = `${window.location.origin}/callback/auth/google`;
    const scope = encodeURIComponent('email profile openid');
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${this.googleClientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}`;
    window.location.href = googleAuthUrl;
  }

  loginWithGithub() {
    sessionStorage.setItem('postLoginRedirect', this.router.url);
    window.location.href = `https://github.com/login/oauth/authorize?client_id=${this.githubClientId}`;
  }

  loginWithFacebook() {
    sessionStorage.setItem('postLoginRedirect', this.router.url);
    const redirectUri = `${window.location.origin}/callback/auth/facebook`;
    const scope = 'email,public_profile';
    const facebookAuthUrl = `https://www.facebook.com/v19.0/dialog/oauth?client_id=${this.facebookClientId}&redirect_uri=${redirectUri}&scope=${scope}&response_type=code`;
    window.location.href = facebookAuthUrl;
  }

  loginWithLinkedin() {
    sessionStorage.setItem('postLoginRedirect', this.router.url);
    const redirectUri = `${window.location.origin}/callback/auth/linkedin`;
    const scope = encodeURIComponent('openid profile email');
    const linkedInAuthUrl = `https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=${this.linkedinClientId}&redirect_uri=${redirectUri}&scope=${scope}`;

    window.location.href = linkedInAuthUrl;
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
