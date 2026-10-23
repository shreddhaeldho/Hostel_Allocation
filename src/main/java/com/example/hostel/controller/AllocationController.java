package com.example.hostel.controller;

import com.example.hostel.dto.AllocationResponse;
import com.example.hostel.service.AllocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Room Allocation and Vacating endpoints.
 * Base URL: http://localhost:8080/api/allocations
 */
@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * Allocates an available room matching the student's preferred room type.
     * POST /api/allocations/{studentId}
     *
     * @param studentId ID of the student from the URL path.
     * @return HTTP 201 Created with allocation details.
     */
    @PostMapping("/{studentId}")
    public ResponseEntity<AllocationResponse> allocateRoom(@PathVariable Long studentId) {
        AllocationResponse response = allocationService.allocateRoom(studentId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all active allocations.
     * GET /api/allocations
     *
     * @return HTTP 200 OK with list of all allocations.
     */
    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllAllocations() {
        List<AllocationResponse> allocations = allocationService.getAllAllocations();
        return ResponseEntity.ok(allocations);
    }

    /**
     * Vacates the room allocated to a student.
     * DELETE /api/allocations/{studentId}
     *
     * @param studentId ID of the student vacating their room.
     * @return HTTP 200 OK with confirmation details.
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Map<String, Object>> vacateRoom(@PathVariable Long studentId) {
        Map<String, Object> response = allocationService.vacateRoom(studentId);
        return ResponseEntity.ok(response);
    }
}
