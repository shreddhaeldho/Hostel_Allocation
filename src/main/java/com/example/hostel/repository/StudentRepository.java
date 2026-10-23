package com.example.hostel.repository;

import com.example.hostel.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Student entity operations.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}
