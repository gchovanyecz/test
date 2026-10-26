/** RFC 9457 ProblemDetail, as returned by the backend for failed requests. */
export interface ProblemFieldError {
  field: string;
  message: string;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  errors?: ProblemFieldError[];
}

export function isProblemDetail(value: unknown): value is ProblemDetail {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Partial<ProblemDetail>;
  return typeof candidate.title === 'string' && typeof candidate.status === 'number';
}
