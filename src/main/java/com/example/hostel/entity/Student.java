package com.example.hostel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing a Student in the Hostel Room Allocation System.
 */
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student name must not be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Department must not be blank")
    @Column(nullable = false)
    private String department;

    @NotBlank(message = "Preferred room type must not be blank")
    @Column(name = "preferred_room_type", nullable = false)
    private String preferredRoomType;

    // Default constructor (required by JPA)
    public Student() {
    }

    // Parameterized constructors
    public Student(String name, String department, String preferredRoomType) {
        this.name = name;
        this.department = department;
        this.preferredRoomType = preferredRoomType;
    }

    public Student(Long id, String name, String department, String preferredRoomType) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.preferredRoomType = preferredRoomType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", preferredRoomType='" + preferredRoomType + '\'' +
                '}';
    }
}
