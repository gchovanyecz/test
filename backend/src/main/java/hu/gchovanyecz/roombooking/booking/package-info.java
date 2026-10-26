/**
 * Room bookings, including the rule that bookings of the same room must not overlap.
 * <p>
 * Layers: {@code controller} (REST, DTOs only) → {@code service} (business rules) → {@code repository} (persistence),
 * with JPA entities in {@code domain} and request/response records in {@code dto}.
 * Administrative endpoints live under {@code /api/admin}.
 */
package hu.gchovanyecz.roombooking.booking;
