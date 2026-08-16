import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UpdateUserCommand, UserDetails } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = '/api/users';
  private http = inject(HttpClient);

  getUserDetails(userId: string): Observable<UserDetails> {
    return this.http.get<UserDetails>(`${this.apiUrl}/${userId}`);
  }

  updateUser(userId: string, command: UpdateUserCommand): Observable<UserDetails> {
    return this.http.patch<UserDetails>(`${this.apiUrl}/${userId}`, command);
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }
}
