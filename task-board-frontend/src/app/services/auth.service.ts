import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthenticationResultDto } from '../models/board.model';
import { finalize } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private authUrl = '/api/authentication';

  private storeAuthorizationData(data: AuthenticationResultDto) {
    this.authorizationData.set(data);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userDisplayName', data.userDisplayName);
    localStorage.setItem('authProvider', data.authProvider);
  }

  private clearAuthorizationData() {
    this.authorizationData.set(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userDisplayName');
    localStorage.removeItem('authProvider');

  }

  authorizationData = signal<AuthenticationResultDto | null>(this.initialAuthData());
  isLoggedIn = computed(() => !!this.authorizationData()?.accessToken);

  private initialAuthData(): AuthenticationResultDto | null {
    const token = localStorage.getItem('accessToken');
    const displayName = localStorage.getItem('userDisplayName');
    const authProvider = localStorage.getItem('authProvider');
    if (token) {
      return { accessToken: token, userDisplayName: displayName || '', authProvider: authProvider || '' };
    }
    return null;
  }

  getToken() {
    return this.authorizationData()?.accessToken;
  }

  login(username: string, password: string): Observable<AuthenticationResultDto> {
    return this.http
      .post<AuthenticationResultDto>(this.authUrl, { username, password }, { withCredentials: true })
      .pipe(tap((response) => this.storeAuthorizationData(response)));
  }

  loginWithExternalProvider(provider: string, token: string): Observable<any> {
    return this.http
      .post<AuthenticationResultDto>(
        this.authUrl + '/oauth2/' + provider,
        { token },
        { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          this.storeAuthorizationData(response);
        }),
      );
  }

  refreshToken(): Observable<AuthenticationResultDto> {
    return this.http
      .post<AuthenticationResultDto>(this.authUrl + '/refresh', {}, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.storeAuthorizationData(response);
        }),
      );
  }

  logout(): Observable<any> {
    return this.http
      .post<void>(this.authUrl + '/logout', {}, { withCredentials: true })
      .pipe(
        finalize(() => {
          this.clearAuthorizationData();
        })
      )
  }
}
