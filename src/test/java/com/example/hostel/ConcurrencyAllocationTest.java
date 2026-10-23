package com.example.hostel;

import com.example.hostel.dto.AllocationResponse;
import com.example.hostel.entity.Room;
import com.example.hostel.entity.Student;
import com.example.hostel.exception.AllocationConflictException;
import com.example.hostel.repository.AllocationRepository;
import com.example.hostel.repository.RoomRepository;
import com.example.hostel.repository.StudentRepository;
import com.example.hostel.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyAllocationTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @BeforeEach
    void setUp() {
        allocationRepository.deleteAll();
        studentRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrency: Simultaneous requests competing for 1 bed cannot over-allocate room capacity")
    void testConcurrentAllocationCompetingForFinalBed() throws InterruptedException {
        // Room with capacity = 1
        Room room = roomRepository.save(new Room("Single", 1));

        int numberOfStudents = 5;
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= numberOfStudents; i++) {
            students.add(studentRepository.save(new Student("Concurrent Student " + i, "Dept", "Single")));
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfStudents);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfStudents);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Exception> otherErrors = Collections.synchronizedList(new ArrayList<>());

        for (Student student : students) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to align
                    AllocationResponse response = allocationService.allocateRoom(student.getId());
                    if (response != null) {
                        successCount.incrementAndGet();
                    }
                } catch (AllocationConflictException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    otherErrors.add(ex);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verification
        assertEquals(true, completed, "All threads should finish within timeout");
        assertEquals(1, successCount.get(), "Exactly 1 student should succeed in claiming the only bed");
        assertEquals(numberOfStudents - 1, conflictCount.get(), "Remaining students should receive AllocationConflictException");
        assertEquals(0, otherErrors.size(), "There should be no unhandled errors during concurrency test");

        // Verify database integrity
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(1, updatedRoom.getOccupiedBeds(), "Room occupancy must never exceed its capacity of 1");
        assertEquals(1, allocationRepository.count(), "Only 1 allocation record must be saved in database");
    }
}
