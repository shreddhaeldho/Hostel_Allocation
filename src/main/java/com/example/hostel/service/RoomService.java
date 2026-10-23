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
    private final com.example.hostel.repository.AllocationRepository allocationRepository;

    public RoomService(RoomRepository roomRepository,
                       com.example.hostel.repository.AllocationRepository allocationRepository) {
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Adds a new hostel room after validating inputs.
     * Newly added rooms always initialize occupied beds to zero.
     *
     * @param room The room object to save.
     * @return The persisted room with generated ID.
     */
    public Room addRoom(Room room) {
        validateRoomInput(room);
        room.setRoomType(room.getRoomType().trim());

        // Always initialize occupied beds to 0 for a newly added room
        room.setOccupiedBeds(0);

        return roomRepository.save(room);
    }

    /**
     * Updates an existing room's roomType or capacity.
     *
     * @param id The room ID.
     * @param updatedRoom Updated room details.
     * @return Updated room entity.
     */
    public Room updateRoom(Long id, Room updatedRoom) {
        Room existing = getRoomById(id);
        validateRoomInput(updatedRoom);

        if (updatedRoom.getCapacity() < existing.getOccupiedBeds()) {
            throw new IllegalArgumentException(
                    "New capacity (" + updatedRoom.getCapacity() + ") cannot be less than currently occupied beds (" + existing.getOccupiedBeds() + ")"
            );
        }

        existing.setRoomType(updatedRoom.getRoomType().trim());
        existing.setCapacity(updatedRoom.getCapacity());

        return roomRepository.save(existing);
    }

    /**
     * Deletes a room by its ID.
     * A room cannot be deleted if it has occupied beds or active allocations.
     *
     * @param id The ID of the room.
     */
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        if (room.getOccupiedBeds() > 0 || allocationRepository.existsByRoomId(id)) {
            throw new com.example.hostel.exception.AllocationConflictException(
                    "Cannot delete room with ID " + id + " because it currently has active allocations or occupied beds."
            );
        }
        roomRepository.delete(room);
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

    private void validateRoomInput(Room room) {
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
    }
}
