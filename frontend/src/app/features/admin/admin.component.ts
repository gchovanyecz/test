import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  template: `
    <section class="page">
      <h1>Admin</h1>
      <p class="page__placeholder">Az adminisztrációs felület hamarosan elérhető lesz.</p>
    </section>
  `,
  styles: `
    .page__placeholder {
      color: var(--color-text-muted);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminComponent {}
