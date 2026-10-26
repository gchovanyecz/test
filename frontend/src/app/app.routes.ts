import { Routes } from '@angular/router';
import { RoomsComponent } from './features/rooms/rooms.component';
import { BookingsComponent } from './features/bookings/bookings.component';
import { AdminComponent } from './features/admin/admin.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'bookings' },
  { path: 'rooms', component: RoomsComponent, title: 'Tárgyalók' },
  { path: 'bookings', component: BookingsComponent, title: 'Foglalások' },
  { path: 'admin', component: AdminComponent, title: 'Admin' },
  { path: '**', redirectTo: 'bookings' },
];
