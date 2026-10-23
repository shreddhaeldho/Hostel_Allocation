package com.example.hostel.service;

import com.example.hostel.entity.Room;
import com.example.hostel.exception.ResourceNotFoundException;
import com.example.hostel.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class handling Hostel Room business logic and validations.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Adds a new hostel room after validating inputs.
     * Newly added rooms always initialize occupied beds to zero.
     *
     * @param room The room object to save.
     * @return The persisted room with generated ID.
     */
    public Room addRoom(Room room) {
        if (room.getRoomType() == null || room.getRoomType().trim().isEmpty()) {
            throw new IllegalArgumentException("Room type must not be blank");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        if (room.getOccupiedBeds() != null && room.getOccupiedBeds() < 0) {
            throw new IllegalArgumentException("Occupied beds must be zero or greater");
        }
        if (room.getOccupiedBeds() != null && room.getOccupiedBeds() > room.getCapacity()) {
            throw new IllegalArgumentException("Occupied beds cannot exceed room capacity");
        }

        room.setRoomType(room.getRoomType().trim());

        // Always initialize occupied beds to 0 for a newly added room
        room.setOccupiedBeds(0);

        return roomRepository.save(room);
    }

    /**
     * Retrieves all rooms in the hostel.
     *
     * @return List of all rooms.
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Retrieves a room by its primary key ID.
     *
     * @param id The ID of the room.
     * @return The Room entity if found.
     * @throws ResourceNotFoundException if room does not exist.
     */
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
    }
}
