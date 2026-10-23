package com.example.hostel.controller;

import com.example.hostel.entity.Student;
import com.example.hostel.service.StudentService;
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
 * REST Controller for Student operations.
 * Base URL: http://localhost:8080/api/students
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Registers a new student.
     * POST /api/students
     *
     * @param student Incoming Student JSON payload.
     * @return HTTP 201 Created with saved Student data.
     */
    @PostMapping
    public ResponseEntity<Student> registerStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.registerStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    /**
     * Retrieves all registered students.
     * GET /api/students
     *
     * @return HTTP 200 OK with list of students.
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieves a student by their ID.
     * GET /api/students/{id}
     *
     * @param id Student ID.
     * @return HTTP 200 OK with Student data if found, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    /**
     * Updates an existing student.
     * PUT /api/students/{id}
     *
     * @param id Student ID.
     * @param student Updated Student payload.
     * @return HTTP 200 OK with updated Student data.
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(id, student);
        return ResponseEntity.ok(updatedStudent);
    }

    /**
     * Deletes a student by ID.
     * DELETE /api/students/{id}
     *
     * @param id Student ID.
     * @return HTTP 200 OK with deletion confirmation.
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, Object>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Student deleted successfully",
                "studentId", id
        ));
    }
}
