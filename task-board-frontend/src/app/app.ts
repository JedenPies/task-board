import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { LoaderComponent } from './components/loader/loader.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, LoaderComponent, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  title = 'task-board-frontend';
  readonly currentYear = new Date().getFullYear();
}
