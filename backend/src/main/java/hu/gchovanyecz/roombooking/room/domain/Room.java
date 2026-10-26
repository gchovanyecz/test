package hu.gchovanyecz.roombooking.room.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A bookable meeting room. Rooms are seeded by a migration and are read-only for the application.
 */
@Entity
@Table(name = "room")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(nullable = false)
	private int capacity;

	@Column(nullable = false, length = 100)
	private String location;

	protected Room() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}

	public String getLocation() {
		return location;
	}

}
