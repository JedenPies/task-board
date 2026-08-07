import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  if (req.url.includes('/api/authentication')) {
    return next(req);
  }

  const token = authService.getToken();
  let clonedReq = req;

  if (token) {
    clonedReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && token) {
        return authService.refreshToken().pipe(
          switchMap((newTokens) => {
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newTokens.accessToken}` },
            });
            return next(retriedReq);
          }),
          catchError((refreshErr) => {
            console.error("Refresh token wygasł. Czyste wylogowanie.");
            authService.logout();
            return throwError(() => refreshErr);
          }),
        );
      }

      return throwError(() => error);
    }),
  );
};
