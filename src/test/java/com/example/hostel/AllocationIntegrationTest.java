package com.example.hostel;

import com.example.hostel.entity.Room;
import com.example.hostel.entity.Student;
import com.example.hostel.repository.AllocationRepository;
import com.example.hostel.repository.RoomRepository;
import com.example.hostel.repository.StudentRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AllocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("POST /api/allocations/{studentId} - Successfully allocate room matching preference")
    void testSuccessfulAllocation() throws Exception {
        Student student = studentRepository.save(new Student("Kavya", "IT", "Single"));
        Room room = roomRepository.save(new Room("Single", 1));

        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allocationId").isNumber())
                .andExpect(jsonPath("$.studentId").value(student.getId()))
                .andExpect(jsonPath("$.studentName").value("Kavya"))
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.roomType").value("Single"))
                .andExpect(jsonPath("$.occupiedBeds").value(1))
                .andExpect(jsonPath("$.capacity").value(1));

        // Verify in repository
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(1, updatedRoom.getOccupiedBeds());
        assertEquals(1, allocationRepository.count());
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Allocate multiple students to same room up to capacity")
    void testAllocateMultipleStudentsToSameRoom() throws Exception {
        Room doubleRoom = roomRepository.save(new Room("Double", 2));
        Student s1 = studentRepository.save(new Student("Student 1", "CSE", "Double"));
        Student s2 = studentRepository.save(new Student("Student 2", "ECE", "Double"));

        // Allocate student 1
        mockMvc.perform(post("/api/allocations/" + s1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occupiedBeds").value(1));

        // Allocate student 2
        mockMvc.perform(post("/api/allocations/" + s2.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occupiedBeds").value(2));

        Room updatedRoom = roomRepository.findById(doubleRoom.getId()).orElseThrow();
        assertEquals(2, updatedRoom.getOccupiedBeds());
        assertEquals(2, allocationRepository.count());
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Full room is never selected; rejects with 409 Conflict")
    void testFullRoomNeverSelected() throws Exception {
        Room singleRoom = roomRepository.save(new Room("Single", 1));
        Student s1 = studentRepository.save(new Student("Student 1", "CSE", "Single"));
        Student s2 = studentRepository.save(new Student("Student 2", "ME", "Single"));

        // S1 claims the only bed
        mockMvc.perform(post("/api/allocations/" + s1.getId()))
                .andExpect(status().isCreated());

        // S2 tries to allocate
        mockMvc.perform(post("/api/allocations/" + s2.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("No available room matches the student's preferred room type")));

        // Occupancy should remain 1, not 2
        Room updatedRoom = roomRepository.findById(singleRoom.getId()).orElseThrow();
        assertEquals(1, updatedRoom.getOccupiedBeds());
        assertEquals(1, allocationRepository.count());
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Student cannot receive two active allocations (409 Conflict)")
    void testDuplicateAllocationPrevented() throws Exception {
        Room room = roomRepository.save(new Room("Double", 2));
        Student student = studentRepository.save(new Student("Ananya", "CSE", "Double"));

        // First allocation succeeds
        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isCreated());

        // Second allocation fails with 409 Conflict
        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("Student already has an active room allocation")));

        // Occupancy must remain 1
        Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(1, updatedRoom.getOccupiedBeds());
        assertEquals(1, allocationRepository.count());
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Unavailable preferred room type produces 409 Conflict")
    void testUnavailableRoomType() throws Exception {
        roomRepository.save(new Room("Single", 1));
        Student student = studentRepository.save(new Student("Rahul", "AI", "Deluxe AC"));

        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("No available room matches the student's preferred room type: Deluxe AC")));
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Nonexistent student returns 404 Not Found")
    void testNonexistentStudentAllocation() throws Exception {
        mockMvc.perform(post("/api/allocations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Student not found with ID: 9999")));
    }

    @Test
    @DisplayName("POST /api/allocations/{studentId} - Smart allocation algorithm selects room with most free beds")
    void testSmartAllocationPreferenceRule() throws Exception {
        // Room 1: capacity 2, occupied 1 -> 1 free bed
        Room room1 = roomRepository.save(new Room("Double", 2));
        room1.setOccupiedBeds(1);
        roomRepository.save(room1);

        // Room 2: capacity 3, occupied 0 -> 3 free beds
        Room room2 = roomRepository.save(new Room("Double", 3));

        Student student = studentRepository.save(new Student("Suresh", "CSE", "Double"));

        // Allocation should choose Room 2 (has 3 free beds vs 1 free bed in Room 1)
        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(room2.getId()))
                .andExpect(jsonPath("$.occupiedBeds").value(1));

        Room updatedRoom2 = roomRepository.findById(room2.getId()).orElseThrow();
        assertEquals(1, updatedRoom2.getOccupiedBeds());
    }

    @Test
    @DisplayName("DELETE /api/allocations/{studentId} - Vacate allocated room and release bed")
    void testVacateRoom() throws Exception {
        Room room = roomRepository.save(new Room("Single", 1));
        Student student = studentRepository.save(new Student("Kiran", "EE", "Single"));

        // Allocate
        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isCreated());

        Room allocatedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(1, allocatedRoom.getOccupiedBeds());

        // Vacate
        mockMvc.perform(delete("/api/allocations/" + student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Room vacated successfully"))
                .andExpect(jsonPath("$.studentId").value(student.getId()))
                .andExpect(jsonPath("$.studentName").value("Kiran"))
                .andExpect(jsonPath("$.roomId").value(room.getId()))
                .andExpect(jsonPath("$.updatedOccupiedBeds").value(0));

        // Confirm database record was removed and occupancy decreased
        assertEquals(0, allocationRepository.count());
        Room vacatedRoom = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(0, vacatedRoom.getOccupiedBeds());

        // Re-vacating returns 404 Not Found
        mockMvc.perform(delete("/api/allocations/" + student.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Released bed can now be re-allocated
        mockMvc.perform(post("/api/allocations/" + student.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occupiedBeds").value(1));
    }

    @Test
    @DisplayName("DELETE /api/allocations/{studentId} - Vacating nonexistent student returns 404")
    void testVacateNonexistentStudent() throws Exception {
        mockMvc.perform(delete("/api/allocations/7777"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/allocations - Retrieve all allocations")
    void testGetAllAllocations() throws Exception {
        Room r1 = roomRepository.save(new Room("Single", 1));
        Room r2 = roomRepository.save(new Room("Double", 2));
        Student s1 = studentRepository.save(new Student("Student 1", "CSE", "Single"));
        Student s2 = studentRepository.save(new Student("Student 2", "ECE", "Double"));

        mockMvc.perform(post("/api/allocations/" + s1.getId())).andExpect(status().isCreated());
        mockMvc.perform(post("/api/allocations/" + s2.getId())).andExpect(status().isCreated());

        mockMvc.perform(get("/api/allocations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].studentName", notNullValue()))
                .andExpect(jsonPath("$[1].studentName", notNullValue()));
    }
}
