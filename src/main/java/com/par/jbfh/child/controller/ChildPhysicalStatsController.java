package com.par.jbfh.child.controller;

import com.par.jbfh.child.dto.CreatePhysicalStatsRequest;
import com.par.jbfh.child.dto.PhysicalStatsResponse;
import com.par.jbfh.child.service.ChildPhysicalStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/children/{childId}/stats")
@RequiredArgsConstructor
@Tag(name = "Child Physical Stats", description = "Child height/weight tracking API")
public class ChildPhysicalStatsController {

    private final ChildPhysicalStatsService statsService;

    @PostMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Add measurement", description = "Record height and weight for a child")
    @ResponseStatus(HttpStatus.CREATED)
    public PhysicalStatsResponse create(@PathVariable UUID childId,
                                        @Valid @RequestBody CreatePhysicalStatsRequest request) {
        return statsService.create(childId, request);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get history", description = "Get height/weight history sorted by date DESC")
    public List<PhysicalStatsResponse> getAll(@PathVariable UUID childId) {
        return statsService.getByChild(childId);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Delete measurement", description = "Remove a measurement record")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID childId, @PathVariable UUID id) {
        statsService.delete(childId, id);
    }
}