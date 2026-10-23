package com.example.hostel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root Controller to confirm backend health and provide API directory.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "system", "Smart Hostel Room Allocation System",
                "studentsEndpoint", "/api/students",
                "roomsEndpoint", "/api/rooms",
                "allocationsEndpoint", "/api/allocations"
        ));
    }
}
