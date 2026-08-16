import { Routes } from '@angular/router';
import { AuthCallbackComponent } from './components/auth-callback/auth-callback.component';
import { ProfileComponent } from './components/profile/profile.component';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy.component';


export const routes: Routes = [

  {
    path: '',
    loadComponent: () => import('./components/board-list/board-list.component').then(m => m.BoardListComponent)
  },
  {
    path: 'board/:id',
    loadComponent: () => import('./components/board/board.component').then(m => m.BoardComponent)
  },
  {
    path: 'callback/auth/:provider', component: AuthCallbackComponent
  },
  {
    path: 'profile', component: ProfileComponent
  },
  {
    path: 'privacy', component: PrivacyPolicyComponent
  }

];
