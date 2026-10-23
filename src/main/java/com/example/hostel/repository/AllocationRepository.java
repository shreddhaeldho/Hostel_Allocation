package com.example.hostel.repository;

import com.example.hostel.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Allocation entity operations.
 */
@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    /**
     * Finds active allocation for a given student ID.
     *
     * @param studentId ID of the student.
     * @return Optional containing the Allocation if found.
     */
    Optional<Allocation> findByStudentId(Long studentId);

    /**
     * Checks if an active allocation exists for a given student ID.
     *
     * @param studentId ID of the student.
     * @return true if allocation exists, false otherwise.
     */
    boolean existsByStudentId(Long studentId);

    /**
     * Checks if an active allocation exists for a given room ID.
     *
     * @param roomId ID of the room.
     * @return true if allocation exists, false otherwise.
     */
    boolean existsByRoomId(Long roomId);

    /**
     * Deletes the allocation record for a given student ID.
     *
     * @param studentId ID of the student.
     */
    void deleteByStudentId(Long studentId);
}
