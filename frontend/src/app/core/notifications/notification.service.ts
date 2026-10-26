import { Injectable, signal } from '@angular/core';

export type NotificationKind = 'error' | 'info';

export interface AppNotification {
  id: number;
  kind: NotificationKind;
  message: string;
}

const AUTO_DISMISS_MS = 6000;

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  private readonly items = signal<AppNotification[]>([]);

  /** Currently visible notifications, oldest first. */
  readonly notifications = this.items.asReadonly();

  error(message: string): void {
    this.push('error', message);
  }

  info(message: string): void {
    this.push('info', message);
  }

  dismiss(id: number): void {
    this.items.update((list) => list.filter((item) => item.id !== id));
  }

  private push(kind: NotificationKind, message: string): void {
    const id = this.nextId++;
    this.items.update((list) => [...list, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), AUTO_DISMISS_MS);
  }
}
