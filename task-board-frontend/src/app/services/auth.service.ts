import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthorizationResultDto } from '../models/board.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private authUrl = '/api/authentication';

  private storeAuthorizationData(data: AuthorizationResultDto) {
    this.authorizationData.set(data);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userDisplayName', data.userDisplayName);
  }

  private clearAuthorizationData() {
    this.authorizationData.set(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userDisplayName');
  }

  authorizationData = signal<AuthorizationResultDto | null>(this.initialAuthData());
  isLoggedIn = computed(() => !!this.authorizationData()?.accessToken);

  private initialAuthData(): AuthorizationResultDto | null {
    const token = localStorage.getItem('accessToken');
    const displayName = localStorage.getItem('userDisplayName');
    if (token) {
      return { accessToken: token, userDisplayName: displayName || '' };
    }
    return null;
  }

  getToken() {
    return this.authorizationData()?.accessToken;
  }

  login(username: string, password: string): Observable<AuthorizationResultDto> {
    return this.http
      .post<AuthorizationResultDto>(this.authUrl, { username, password }, { withCredentials: true })
      .pipe(tap((response) => this.storeAuthorizationData(response)));
  }

  loginWithExternalProvider(provider: string, token: string): Observable<any> {
    return this.http
      .post<AuthorizationResultDto>(
        this.authUrl + '/oauth2/' + provider.toUpperCase(),
        { token },
        { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          this.storeAuthorizationData(response);
        }),
      );
  }

  loginWithGoogle(token: string): Observable<any> {
    return this.loginWithExternalProvider('google', token);
  }

  refreshToken(): Observable<AuthorizationResultDto> {
    return this.http
      .post<AuthorizationResultDto>(this.authUrl + '/refresh', {}, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.storeAuthorizationData(response);
        }),
      );
  }

  logout(): void {
    this.clearAuthorizationData();
  }
}
