import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-rooms',
  template: `
    <section class="page">
      <h1>Tárgyalók</h1>
      <p class="page__placeholder">A tárgyalók listája hamarosan elérhető lesz.</p>
    </section>
  `,
  styles: `
    .page__placeholder {
      color: var(--color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoomsComponent {}
