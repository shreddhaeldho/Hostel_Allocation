package com.example.hostel.controller;

import com.example.hostel.entity.Room;
import com.example.hostel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Hostel Room operations.
 * Base URL: http://localhost:8080/api/rooms
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Adds a new hostel room.
     * POST /api/rooms
     *
     * @param room Incoming Room JSON payload.
     * @return HTTP 201 Created with saved Room data.
     */
    @PostMapping
    public ResponseEntity<Room> addRoom(@Valid @RequestBody Room room) {
        Room createdRoom = roomService.addRoom(room);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    /**
     * Retrieves all hostel rooms.
     * GET /api/rooms
     *
     * @return HTTP 200 OK with list of rooms.
     */
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Retrieves a room by its ID.
     * GET /api/rooms/{id}
     *
     * @param id Room ID.
     * @return HTTP 200 OK with Room data if found, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * Updates an existing room.
     * PUT /api/rooms/{id}
     *
     * @param id Room ID.
     * @param room Updated Room payload.
     * @return HTTP 200 OK with updated Room data.
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
        Room updatedRoom = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * Deletes a room by ID.
     * DELETE /api/rooms/{id}
     *
     * @param id Room ID.
     * @return HTTP 200 OK with deletion confirmation.
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, Object>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Room deleted successfully",
                "roomId", id
        ));
    }
}
