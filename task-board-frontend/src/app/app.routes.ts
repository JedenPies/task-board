import { Routes } from '@angular/router';

export const routes: Routes = [

  {
    path: '',
    loadComponent: () => import('./components/board-list/board-list').then(m => m.BoardList)
  },
  {
    path: 'board/:id',
    loadComponent: () => import('./components/board/board').then(m => m.Board)
  }

];
