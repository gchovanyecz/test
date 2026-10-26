export interface BookingResponse {
  id: number;
  roomId: number;
  roomName: string;
  title: string | null;
  /** ISO-8601 instant */
  startTime: string;
  /** ISO-8601 instant */
  endTime: string;
  /** ISO-8601 instant */
  createdAt: string;
}

export interface CreateBookingRequest {
  roomId: number;
  title?: string | null;
  /** ISO-8601 instant */
  startTime: string;
  /** ISO-8601 instant */
  endTime: string;
}

export interface BookingQuery {
  roomId?: number;
  /** ISO-8601 instant */
  from?: string;
  /** ISO-8601 instant */
  to?: string;
}
