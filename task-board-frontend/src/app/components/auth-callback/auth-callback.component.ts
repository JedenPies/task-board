import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  template: `
    <div style="display: flex; justify-content: center; margin-top: 50px;">
      <h2>Przetwarzanie logowania...</h2>
    </div>
  `,
})
export class AuthCallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  ngOnInit() {
    const provider = this.route.snapshot.paramMap.get('provider');
    const code = this.route.snapshot.queryParamMap.get('code');
    if (provider && code) {
      this.authService.loginWithExternalProvider(provider, code).subscribe({
        next: (response) => {
          const returnUrl = sessionStorage.getItem('postLoginRedirect') || '/';
          sessionStorage.removeItem('postLoginRedirect'); // sprzątamy
          this.router.navigateByUrl(returnUrl);
        },
        error: (err) => {
          console.error('Błąd logowania przez GitHuba:', err);
          this.router.navigate(['/']);
        },
      });
    } else {
      this.router.navigate(['/']);
    }
  }
}
