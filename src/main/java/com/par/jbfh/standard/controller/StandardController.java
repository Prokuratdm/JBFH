package com.par.jbfh.standard.controller;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.dto.CreateStandardRequest;
import com.par.jbfh.standard.dto.StandardResponse;
import com.par.jbfh.standard.dto.UpdateStandardRequest;
import com.par.jbfh.standard.service.StandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/standards")
@RequiredArgsConstructor
@Tag(name = "Standards", description = "Standards management API")
public class StandardController {

    private final StandardService standardService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Create standard")
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse create(@Valid @RequestBody CreateStandardRequest request) {
        return standardService.create(request);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB_METHODIST",
            "ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List standards", description = "Filter by birthYear and/or type")
    public Page<StandardResponse> getAll(@RequestParam(required = false) Integer birthYear,
                                         @RequestParam(required = false) ExerciseType type,
                                         @PageableDefault(size = 20) Pageable pageable) {
        return standardService.getAll(birthYear, type, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB_METHODIST",
            "ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get standard")
    public StandardResponse getById(@PathVariable UUID id) {
        return standardService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Update standard")
    public StandardResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateStandardRequest request) {
        return standardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Delete standard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        standardService.delete(id);
    }
}