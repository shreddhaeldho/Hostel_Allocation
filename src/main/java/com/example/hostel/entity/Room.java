package com.example.hostel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing a Hostel Room.
 * Tracks total capacity and currently occupied beds.
 */
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room type must not be blank")
    @Column(name = "room_type", nullable = false)
    private String roomType;

    @NotNull(message = "Capacity must not be null")
    @Min(value = 1, message = "Capacity must be greater than zero")
    @Column(nullable = false)
    private Integer capacity;

    @NotNull(message = "Occupied beds must not be null")
    @Min(value = 0, message = "Occupied beds must be zero or greater")
    @Column(name = "occupied_beds", nullable = false)
    private Integer occupiedBeds = 0;

    // Default constructor (required by JPA)
    public Room() {
    }

    // Parameterized constructors
    public Room(String roomType, Integer capacity) {
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupiedBeds = 0;
    }

    public Room(Long id, String roomType, Integer capacity, Integer occupiedBeds) {
        this.id = id;
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupiedBeds = (occupiedBeds != null) ? occupiedBeds : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", occupiedBeds=" + occupiedBeds +
                '}';
    }
}
