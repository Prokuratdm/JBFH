package com.par.jbfh.location.controller;

import com.par.jbfh.location.dto.CreateLocationRequest;
import com.par.jbfh.location.dto.LocationResponse;
import com.par.jbfh.location.dto.UpdateLocationRequest;
import com.par.jbfh.location.service.LocationService;
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
@RequestMapping("/api/v1/clubs/{clubId}/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Club locations management API")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @Secured("ROLE_CLUB")
    @Operation(summary = "Create location", description = "Create a new location for a club. Club representative only.")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@PathVariable UUID clubId, @Valid @RequestBody CreateLocationRequest request) {
        return locationService.create(clubId, request);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all locations", description = "Returns list of locations for a club. By default only active.")
    public List<LocationResponse> getAll(
            @PathVariable UUID clubId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return locationService.getAll(clubId, includeInactive);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get location by ID")
    public LocationResponse getById(@PathVariable UUID clubId, @PathVariable UUID id) {
        return locationService.getById(clubId, id);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Update location")
    public LocationResponse update(@PathVariable UUID clubId, @PathVariable UUID id, @Valid @RequestBody UpdateLocationRequest request) {
        return locationService.update(clubId, id, request);
    }

    @PatchMapping("/{id}/active")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Activate or deactivate location")
    public LocationResponse setActive(@PathVariable UUID clubId, @PathVariable UUID id, @RequestParam boolean active) {
        return locationService.setActive(clubId, id, active);
    }
}