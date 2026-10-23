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

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Registers a new student after validating inputs.
     *
     * @param student The student object to save.
     * @return The persisted student with generated ID.
     */
    public Student registerStudent(Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name must not be blank");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department must not be blank");
        }
        if (student.getPreferredRoomType() == null || student.getPreferredRoomType().trim().isEmpty()) {
            throw new IllegalArgumentException("Preferred room type must not be blank");
        }

        // Clean input strings
        student.setName(student.getName().trim());
        student.setDepartment(student.getDepartment().trim());
        student.setPreferredRoomType(student.getPreferredRoomType().trim());

        return studentRepository.save(student);
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
}
