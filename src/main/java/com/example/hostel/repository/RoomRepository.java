package com.example.hostel.repository;

import com.example.hostel.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Room entity operations.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Acquires a pessimistic write lock (SELECT ... FOR UPDATE) on the room record
     * to prevent race conditions and concurrent over-allocation.
     *
     * @param id Room ID.
     * @return Locked Room if present.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);
}
