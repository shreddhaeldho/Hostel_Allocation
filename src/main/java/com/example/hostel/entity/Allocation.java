package com.example.hostel.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity representing an Allocation of a Room to a Student.
 * A student can have at most one active room allocation (enforced via unique student_id).
 * A room can host multiple student allocations up to its capacity.
 */
@Entity
@Table(name = "allocation")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id", unique = true, nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = false)
    private Room room;

    // Default constructor (required by JPA)
    public Allocation() {
    }

    // Parameterized constructor
    public Allocation(Student student, Room room) {
        this.student = student;
        this.room = room;
    }

    public Allocation(Long id, Student student, Room room) {
        this.id = id;
        this.student = student;
        this.room = room;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "id=" + id +
                ", student=" + (student != null ? student.getId() : null) +
                ", room=" + (room != null ? room.getId() : null) +
                '}';
    }
}
