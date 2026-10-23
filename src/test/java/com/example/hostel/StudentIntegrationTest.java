package com.example.hostel;

import com.example.hostel.repository.AllocationRepository;
import com.example.hostel.repository.RoomRepository;
import com.example.hostel.repository.StudentRepository;
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
class StudentIntegrationTest {

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
    @DisplayName("POST /api/students - Register a valid student")
    void testRegisterValidStudent() throws Exception {
        String studentJson = """
                {
                    "name": "Aarav Sharma",
                    "department": "Computer Science",
                    "preferredRoomType": "Single"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Aarav Sharma"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.preferredRoomType").value("Single"));
    }

    @Test
    @DisplayName("GET /api/students - Retrieve all registered students")
    void testGetAllStudents() throws Exception {
        // Register two students
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sneha Patel", "department": "Electronics", "preferredRoomType": "Double"}
                        """)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Vikram Rao", "department": "Mechanical", "preferredRoomType": "Single"}
                        """)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Sneha Patel"))
                .andExpect(jsonPath("$[1].name").value("Vikram Rao"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Retrieve student by ID")
    void testGetStudentById() throws Exception {
        String response = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Meera Joshi", "department": "Civil", "preferredRoomType": "Double"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract ID using JsonPath
        Number idNum = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        long studentId = idNum.longValue();

        mockMvc.perform(get("/api/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.name").value("Meera Joshi"))
                .andExpect(jsonPath("$.department").value("Civil"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Request nonexistent student returns 404")
    void testGetNonexistentStudent() throws Exception {
        mockMvc.perform(get("/api/students/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Student not found with ID: 9999")));
    }

    @Test
    @DisplayName("POST /api/students - Submit empty request body returns 400")
    void testSubmitEmptyBody() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/students - Submit blank name returns 400")
    void testSubmitBlankName() throws Exception {
        String invalidJson = """
                {
                    "name": "   ",
                    "department": "CSE",
                    "preferredRoomType": "Single"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/students - Submit missing/blank department returns 400")
    void testSubmitBlankDepartment() throws Exception {
        String invalidJson = """
                {
                    "name": "Rohit Verma",
                    "department": "",
                    "preferredRoomType": "Single"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/students/{id} - Update an existing student")
    void testUpdateStudent() throws Exception {
        com.example.hostel.entity.Student student = studentRepository.save(new com.example.hostel.entity.Student("Old Name", "Old Dept", "Single"));

        String updateJson = """
                {
                    "name": "New Name",
                    "department": "New Dept",
                    "preferredRoomType": "Double"
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.department").value("New Dept"))
                .andExpect(jsonPath("$.preferredRoomType").value("Double"));
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Delete a student without allocation")
    void testDeleteStudentSuccess() throws Exception {
        com.example.hostel.entity.Student student = studentRepository.save(new com.example.hostel.entity.Student("To Delete", "ECE", "Single"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/students/" + student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student deleted successfully"))
                .andExpect(jsonPath("$.studentId").value(student.getId()));

        org.junit.jupiter.api.Assertions.assertFalse(studentRepository.existsById(student.getId()));
    }

    @Test
    @DisplayName("DELETE /api/students/{id} - Delete a student with active allocation fails with 409")
    void testDeleteStudentWithActiveAllocationFails() throws Exception {
        com.example.hostel.entity.Student student = studentRepository.save(new com.example.hostel.entity.Student("Active", "CSE", "Single"));
        com.example.hostel.entity.Room room = roomRepository.save(new com.example.hostel.entity.Room("Single", 1));
        allocationRepository.save(new com.example.hostel.entity.Allocation(student, room));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/students/" + student.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("active room allocation")));
    }
}
