package com.example.hostel.service;

import com.example.hostel.entity.Student;
import com.example.hostel.exception.ResourceNotFoundException;
import com.example.hostel.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class handling Student business logic and validations.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final com.example.hostel.repository.AllocationRepository allocationRepository;

    public StudentService(StudentRepository studentRepository,
                          com.example.hostel.repository.AllocationRepository allocationRepository) {
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Registers a new student after validating inputs.
     *
     * @param student The student object to save.
     * @return The persisted student with generated ID.
     */
    public Student registerStudent(Student student) {
        validateStudentInput(student);

        // Clean input strings
        student.setName(student.getName().trim());
        student.setDepartment(student.getDepartment().trim());
        student.setPreferredRoomType(student.getPreferredRoomType().trim());

        return studentRepository.save(student);
    }

    /**
     * Updates an existing student.
     *
     * @param id The ID of the student to update.
     * @param updatedStudent The updated details.
     * @return The updated Student entity.
     */
    public Student updateStudent(Long id, Student updatedStudent) {
        Student existing = getStudentById(id);
        validateStudentInput(updatedStudent);

        existing.setName(updatedStudent.getName().trim());
        existing.setDepartment(updatedStudent.getDepartment().trim());
        existing.setPreferredRoomType(updatedStudent.getPreferredRoomType().trim());

        return studentRepository.save(existing);
    }

    /**
     * Deletes a student by ID.
     * Cannot delete a student if an active room allocation exists.
     *
     * @param id The ID of the student.
     */
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        if (allocationRepository.existsByStudentId(id)) {
            throw new com.example.hostel.exception.AllocationConflictException(
                    "Cannot delete student with ID " + id + " because they have an active room allocation. Please vacate room first."
            );
        }
        studentRepository.delete(student);
    }

    /**
     * Retrieves all registered students.
     *
     * @return List of all students.
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Retrieves a student by their primary key ID.
     *
     * @param id The ID of the student.
     * @return The Student entity if found.
     * @throws ResourceNotFoundException if student does not exist.
     */
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    private void validateStudentInput(Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name must not be blank");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department must not be blank");
        }
        if (student.getPreferredRoomType() == null || student.getPreferredRoomType().trim().isEmpty()) {
            throw new IllegalArgumentException("Preferred room type must not be blank");
        }
    }
}
