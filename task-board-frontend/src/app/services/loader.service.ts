import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
  })
export class LoaderService {

  public isLoading = signal<boolean>(false);

  private activeRequests = 0;
  private timer: ReturnType<typeof setTimeout> | null = null;

  show() {
    this.activeRequests++;
    if (this.activeRequests === 1 && !this.timer) {
      this.timer = setTimeout(() => {
        if (this.activeRequests > 0) {
          this.isLoading.set(true);
        }
      }, 200);
    }
  }

  hide() {
    this.activeRequests = Math.max(0, this.activeRequests - 1);
    if (this.activeRequests === 0) {
      if (this.timer) {
        clearTimeout(this.timer);
        this.timer = null;
      }
      this.isLoading.set(false);
    }
  }
}
