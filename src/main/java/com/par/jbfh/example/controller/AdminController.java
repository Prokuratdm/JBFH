package com.par.jbfh.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Example", description = "Example controller for ROLE_ADMIN")
public class AdminController {

    @GetMapping("/dashboard")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Admin dashboard", description = "Only accessible by ROLE_ADMIN")
    public ResponseEntity<Map<String, String>> getDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Admin Dashboard",
                "status", "You have ADMIN access"
        ));
    }

    @GetMapping("/stats")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "System stats", description = "Only accessible by ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", 150,
                "totalClubs", 25,
                "activeSessions", 12
        ));
    }
}