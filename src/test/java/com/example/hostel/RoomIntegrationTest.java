package com.example.hostel;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @BeforeEach
    void setUp() {
        allocationRepository.deleteAll();
        studentRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/rooms - Add room with valid capacity and verify 0 occupied beds default")
    void testAddValidRoom() throws Exception {
        String roomJson = """
                {
                    "roomType": "Double",
                    "capacity": 2
                }
                """;

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roomType").value("Double"))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.occupiedBeds").value(0));
    }

    @Test
    @DisplayName("POST /api/rooms - Add rooms of different room types")
    void testAddDifferentRoomTypes() throws Exception {
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomType": "Single", "capacity": 1}
                        """)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomType": "Double", "capacity": 2}
                        """)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomType": "Deluxe AC", "capacity": 3}
                        """)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].roomType").value("Single"))
                .andExpect(jsonPath("$[1].roomType").value("Double"))
                .andExpect(jsonPath("$[2].roomType").value("Deluxe AC"));
    }

    @Test
    @DisplayName("GET /api/rooms/{id} - Retrieve room by ID")
    void testGetRoomById() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomType": "Single", "capacity": 1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number idNum = JsonPath.read(response, "$.id");
        long roomId = idNum.longValue();

        mockMvc.perform(get("/api/rooms/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId))
                .andExpect(jsonPath("$.roomType").value("Single"))
                .andExpect(jsonPath("$.capacity").value(1))
                .andExpect(jsonPath("$.occupiedBeds").value(0));
    }

    @Test
    @DisplayName("GET /api/rooms/{id} - Nonexistent room returns 404")
    void testGetNonexistentRoom() throws Exception {
        mockMvc.perform(get("/api/rooms/8888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Room not found with ID: 8888")));
    }

    @Test
    @DisplayName("POST /api/rooms - Zero capacity is rejected with 400 Bad Request")
    void testRejectZeroCapacity() throws Exception {
        String invalidJson = """
                {
                    "roomType": "Single",
                    "capacity": 0
                }
                """;

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/rooms - Negative capacity is rejected with 400 Bad Request")
    void testRejectNegativeCapacity() throws Exception {
        String invalidJson = """
                {
                    "roomType": "Double",
                    "capacity": -2
                }
                """;

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/rooms - Negative occupied beds is rejected with 400 Bad Request")
    void testRejectNegativeOccupiedBeds() throws Exception {
        String invalidJson = """
                {
                    "roomType": "Double",
                    "capacity": 2,
                    "occupiedBeds": -1
                }
                """;

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/rooms - Occupied beds greater than capacity is rejected with 400 Bad Request")
    void testRejectOccupiedBedsGreaterThanCapacity() throws Exception {
        String invalidJson = """
                {
                    "roomType": "Double",
                    "capacity": 2,
                    "occupiedBeds": 5
                }
                """;

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} - Update room details")
    void testUpdateRoom() throws Exception {
        com.example.hostel.entity.Room room = roomRepository.save(new com.example.hostel.entity.Room("Single", 1));

        String updateJson = """
                {
                    "roomType": "Double",
                    "capacity": 2
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/rooms/" + room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(room.getId()))
                .andExpect(jsonPath("$.roomType").value("Double"))
                .andExpect(jsonPath("$.capacity").value(2));
    }

    @Test
    @DisplayName("DELETE /api/rooms/{id} - Delete an empty room")
    void testDeleteEmptyRoomSuccess() throws Exception {
        com.example.hostel.entity.Room room = roomRepository.save(new com.example.hostel.entity.Room("Single", 1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/rooms/" + room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Room deleted successfully"))
                .andExpect(jsonPath("$.roomId").value(room.getId()));

        org.junit.jupiter.api.Assertions.assertFalse(roomRepository.existsById(room.getId()));
    }

    @Test
    @DisplayName("DELETE /api/rooms/{id} - Delete room with active occupants fails with 409")
    void testDeleteOccupiedRoomFails() throws Exception {
        com.example.hostel.entity.Room room = roomRepository.save(new com.example.hostel.entity.Room("Single", 1));
        room.setOccupiedBeds(1);
        roomRepository.save(room);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/rooms/" + room.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("active allocations or occupied beds")));
    }
}
