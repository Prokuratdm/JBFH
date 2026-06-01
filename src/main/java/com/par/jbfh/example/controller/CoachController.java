package com.par.jbfh.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coach")
@Tag(name = "Coach Example", description = "Example controller for ROLE_COACH and ROLE_MAIN_COACH")
public class CoachController {

    @GetMapping("/my-team")
    @Secured({"ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get my team", description = "Accessible by ROLE_COACH or ROLE_MAIN_COACH")
    public ResponseEntity<Map<String, Object>> getMyTeam() {
        return ResponseEntity.ok(Map.of(
                "teamName", "Hockey Team",
                "players", List.of(
                        Map.of("name", "Player 1", "position", "Forward"),
                        Map.of("name", "Player 2", "position", "Defender")
                ),
                "message", "You have COACH access"
        ));
    }

    @GetMapping("/schedule")
    @Secured({"ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get schedule", description = "Accessible by ROLE_COACH or ROLE_MAIN_COACH")
    public ResponseEntity<Map<String, Object>> getSchedule() {
        return ResponseEntity.ok(Map.of(
                "schedule", List.of(
                        Map.of("date", "2026-06-10", "opponent", "Team A", "location", "Home"),
                        Map.of("date", "2026-06-17", "opponent", "Team B", "location", "Away")
                )
        ));
    }
}