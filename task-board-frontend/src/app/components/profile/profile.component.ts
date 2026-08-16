import { Component, effect, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { Router } from '@angular/router';
import { UserDetails } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  user = signal<UserDetails | null>(null);
  isEditing = signal(false);
  editName = signal('');

  showDeleteConfirm = signal(false);
  deleteConfirmText = signal('');

  constructor() {
    effect(() => {
      const loggedIn = this.authService.isLoggedIn();
      if (!loggedIn) {
        this.router.navigate(['/']);
      }
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  isLoggedIn() {
    return this.authService.isLoggedIn;
  }

  loadProfile() {
    const userId = this.authService.authorizationData()?.userId;
    if (userId) {
      this.userService.getUserDetails(userId).subscribe({
        next: (data) => {
          this.user.set(data);
          this.editName.set(data.displayName);
        },
        error: (err) => console.error('Błąd pobierania profilu', err),
      });
    }
  }

  saveProfile() {
    if (!this.editName().trim()) return;
    const userId = this.authService.authorizationData()?.userId;
    if (userId) {
      this.userService
        .updateUser(userId, { displayName: this.editName() })
        .pipe(
          switchMap((updatedUser) => {
            this.user.set(updatedUser);
            this.isEditing.set(false);
            return this.authService.refreshToken();
          }),
        )
        .subscribe({
          next: (updatedUser) => {},
          error: (err) => {
            console.error('Błąd aktualizacji', err);
            this.isEditing.set(false);
          },
        });
    }
  }

  deleteAccount() {
    if (this.deleteConfirmText().toLowerCase() !== 'delete') return;
    const userId = this.authService.authorizationData()?.userId;
    if (userId) {
      this.userService.deleteUser(userId).subscribe({
        next: () => {
          this.authService.clearAuthorizationData();
          this.router.navigate(['/']);
        },
        error: (err) => console.error('Błąd podczas usuwania konta', err),
      });
    }
  }
}
