package com.example.hostel.dto;

import com.example.hostel.entity.Allocation;

/**
 * Data Transfer Object for Allocation responses.
 * Avoids infinite recursion, circular JSON, and lazy-loading proxy issues.
 */
public class AllocationResponse {

    private Long allocationId;
    private Long studentId;
    private String studentName;
    private String department;
    private Long roomId;
    private String roomType;
    private Integer occupiedBeds;
    private Integer capacity;

    public AllocationResponse() {
    }

    public AllocationResponse(Long allocationId, Long studentId, String studentName, String department,
                              Long roomId, String roomType, Integer occupiedBeds, Integer capacity) {
        this.allocationId = allocationId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.roomId = roomId;
        this.roomType = roomType;
        this.occupiedBeds = occupiedBeds;
        this.capacity = capacity;
    }

    /**
     * Factory method to convert an Allocation entity to an AllocationResponse DTO.
     */
    public static AllocationResponse fromEntity(Allocation allocation) {
        return new AllocationResponse(
                allocation.getId(),
                allocation.getStudent() != null ? allocation.getStudent().getId() : null,
                allocation.getStudent() != null ? allocation.getStudent().getName() : null,
                allocation.getStudent() != null ? allocation.getStudent().getDepartment() : null,
                allocation.getRoom() != null ? allocation.getRoom().getId() : null,
                allocation.getRoom() != null ? allocation.getRoom().getRoomType() : null,
                allocation.getRoom() != null ? allocation.getRoom().getOccupiedBeds() : null,
                allocation.getRoom() != null ? allocation.getRoom().getCapacity() : null
        );
    }

    // Getters and Setters
    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
