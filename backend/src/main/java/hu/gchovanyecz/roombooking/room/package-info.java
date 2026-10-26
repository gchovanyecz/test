/**
 * Meeting rooms. The room list is fixed and seeded by a database migration; there is no room administration.
 * <p>
 * Layers: {@code controller} (REST, DTOs only) → {@code service} (business rules) → {@code repository} (persistence),
 * with JPA entities in {@code domain} and request/response records in {@code dto}.
 */
package hu.gchovanyecz.roombooking.room;
