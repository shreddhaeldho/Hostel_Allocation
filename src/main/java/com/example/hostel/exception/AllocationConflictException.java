package com.example.hostel.exception;

/**
 * Exception thrown when an allocation request conflicts with business rules
 * (e.g., student already has an active allocation, or no room with capacity matches preference).
 * Maps to HTTP 409 Conflict.
 */
public class AllocationConflictException extends RuntimeException {
    public AllocationConflictException(String message) {
        super(message);
    }
}
