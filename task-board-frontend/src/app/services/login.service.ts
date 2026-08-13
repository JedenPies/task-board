import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private router = inject(Router);

  private githubClientId = environment.githubClientId;
  private googleClientId = environment.googleClientId;
  private facebookClientId = environment.facebookClientId;
  private linkedinClientId = environment.linkedinClientId;

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
}
