import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

interface LoginResponse {
  accessToken: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);
  private authUrl = '/api/authentication';

  private tokenSignal = signal<string | null>(localStorage.getItem('token'));

  isLoggedIn = computed(() => !!this.tokenSignal());

  getToken() {
    return this.tokenSignal();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.authUrl, { username, password }, { withCredentials: true }).pipe(
      tap((response) => {
        localStorage.setItem('token', response.accessToken);
        this.tokenSignal.set(response.accessToken);
      }),
    );
  }

  loginWithExternalProvider(provider: string, token: string): Observable<any> {
    return this.http.post<any>(this.authUrl + '/oauth2/' + provider.toUpperCase(), { token }, { withCredentials: true }).pipe(
      tap((response) => {
        localStorage.setItem('token', response.accessToken);
        this.tokenSignal.set(response.accessToken);
      })
    )
  }

  loginWithGoogle(token: string): Observable<any> {
    return this.loginWithExternalProvider('google', token);
  }

  refreshToken(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.authUrl + '/refresh', {}, { withCredentials: true }).pipe(
      tap((response) => {
        localStorage.setItem('token', response.accessToken);
        this.tokenSignal.set(response.accessToken);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem('token')
    this.tokenSignal.set(null)
  }
}
