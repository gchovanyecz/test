import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-bookings',
  template: `
    <section class="page">
      <h1>Foglalások</h1>
      <p class="page__placeholder">A foglalások listája hamarosan elérhető lesz.</p>
    </section>
  `,
  styles: `
    .page__placeholder {
      color: var(--color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingsComponent {}
