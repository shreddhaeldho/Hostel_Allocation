package com.example.hostel.service;

import com.example.hostel.dto.AllocationResponse;
import com.example.hostel.entity.Allocation;
import com.example.hostel.entity.Room;
import com.example.hostel.entity.Student;
import com.example.hostel.exception.AllocationConflictException;
import com.example.hostel.exception.ResourceNotFoundException;
import com.example.hostel.repository.AllocationRepository;
import com.example.hostel.repository.RoomRepository;
import com.example.hostel.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class handling Smart Room Allocation and Vacating logic.
 */
@Service
public class AllocationService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AllocationService(StudentRepository studentRepository,
                             RoomRepository roomRepository,
                             AllocationRepository allocationRepository) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Allocates an available room matching the student's preferred room type.
     *
     * @param studentId ID of the student seeking room allocation.
     * @return AllocationResponse details.
     */
    @Transactional
    public AllocationResponse allocateRoom(Long studentId) {
        // Step 1: Check whether student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Step 2: Check whether student already has an active allocation
        if (allocationRepository.existsByStudentId(studentId)) {
            throw new AllocationConflictException("Student already has an active room allocation.");
        }

        // Step 3: Find candidate eligible rooms matching student's preferred room type
        String preferredType = student.getPreferredRoomType() != null ? student.getPreferredRoomType().trim() : "";

        List<Room> allRooms = roomRepository.findAll();

        List<Room> eligibleRooms = allRooms.stream()
                // Match preferred room type (case-insensitive & trimmed)
                .filter(room -> room.getRoomType() != null && room.getRoomType().trim().equalsIgnoreCase(preferredType))
                // Ensure room has available capacity (occupiedBeds < capacity)
                .filter(room -> room.getOccupiedBeds() < room.getCapacity())
                // Step 4: Sort by most available beds first, tie-break by lowest room ID
                .sorted(Comparator
                        .comparingInt((Room r) -> r.getCapacity() - r.getOccupiedBeds()).reversed()
                        .thenComparing(Room::getId)
                )
                .toList();

        if (eligibleRooms.isEmpty()) {
            throw new AllocationConflictException("No available room matches the student's preferred room type: " + preferredType);
        }

        // Step 5: Acquire pessimistic write lock and refresh room capacity directly from DB
        Room selectedRoom = null;
        for (Room candidate : eligibleRooms) {
            entityManager.refresh(candidate, LockModeType.PESSIMISTIC_WRITE);
            if (candidate.getOccupiedBeds() < candidate.getCapacity()) {
                selectedRoom = candidate;
                break;
            }
        }

        if (selectedRoom == null) {
            throw new AllocationConflictException("No available room matches the student's preferred room type: " + preferredType);
        }

        // Step 6: Update room occupancy and save allocation record
        selectedRoom.setOccupiedBeds(selectedRoom.getOccupiedBeds() + 1);
        roomRepository.saveAndFlush(selectedRoom);

        Allocation allocation = new Allocation(student, selectedRoom);
        Allocation savedAllocation = allocationRepository.saveAndFlush(allocation);

        // Step 7: Return DTO response
        return AllocationResponse.fromEntity(savedAllocation);
    }

    /**
     * Retrieves all active allocations.
     *
     * @return List of AllocationResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll().stream()
                .map(AllocationResponse::fromEntity)
                .toList();
    }

    /**
     * Vacates a room currently allocated to a student.
     *
     * @param studentId ID of the student vacating their room.
     * @return Confirmation details of the vacated room.
     */
    @Transactional
    public Map<String, Object> vacateRoom(Long studentId) {
        // Step 1: Check whether student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Step 2: Find the student's active allocation
        Allocation allocation = allocationRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + studentId + " does not have an active room allocation."));

        Room room = allocation.getRoom();
        int updatedOccupiedBeds = 0;
        if (room != null) {
            // Lock room pessimistically and refresh before updating occupancy
            entityManager.refresh(room, LockModeType.PESSIMISTIC_WRITE);
            // Step 3: Decrease room occupancy (ensure it never drops below 0)
            updatedOccupiedBeds = Math.max(0, room.getOccupiedBeds() - 1);
            room.setOccupiedBeds(updatedOccupiedBeds);
            roomRepository.saveAndFlush(room);
        }

        // Step 4: Delete the allocation record
        allocationRepository.delete(allocation);
        allocationRepository.flush();

        // Step 5: Return confirmation
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room vacated successfully");
        response.put("studentId", studentId);
        response.put("studentName", student.getName());
        response.put("roomId", room != null ? room.getId() : null);
        response.put("updatedOccupiedBeds", updatedOccupiedBeds);
        return response;
    }
}
