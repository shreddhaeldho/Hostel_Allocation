package com.example.hostel.exception;

/**
 * Exception thrown when a requested resource (Student, Room, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
