import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RoomApiService } from '../../core/api/room-api.service';
import { RoomResponse } from '../../core/models/room.model';
import { LoadStatus } from '../../shared/load-status';

@Component({
  selector: 'app-rooms',
  templateUrl: './rooms.component.html',
  styleUrl: './rooms.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoomsComponent {
  private readonly roomApi = inject(RoomApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly rooms = signal<RoomResponse[]>([]);
  protected readonly status = signal<LoadStatus>('loading');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.status.set('loading');
    this.roomApi
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rooms) => {
          this.rooms.set(rooms);
          this.status.set('loaded');
        },
        error: () => this.status.set('error'),
      });
  }
}
