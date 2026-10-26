import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { isProblemDetail } from '../models/problem-detail.model';
import { NotificationService } from '../notifications/notification.service';

export const GENERIC_ERROR_MESSAGE = 'Váratlan hiba történt. Próbálja meg újra később.';

export function errorMessageFor(error: HttpErrorResponse): string {
  const body: unknown = error.error;
  if (isProblemDetail(body) && body.detail?.trim()) {
    return body.detail;
  }
  return GENERIC_ERROR_MESSAGE;
}

/** Shows the ProblemDetail `detail` of every failed HTTP request as an error notification. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        notifications.error(errorMessageFor(error));
      }
      return throwError(() => error);
    }),
  );
};
