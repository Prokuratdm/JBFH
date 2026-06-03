package com.par.jbfh.child.controller;

import com.par.jbfh.child.dto.ChildStandardResponse;
import com.par.jbfh.child.dto.CreateChildStandardRequest;
import com.par.jbfh.child.service.ChildStandardService;
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
@RequestMapping("/api/v1/children/{childId}/standards")
@RequiredArgsConstructor
@Tag(name = "Child Standards", description = "Child standard results API")
public class ChildStandardController {

    private final ChildStandardService childStandardService;

    @PostMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Record result", description = "Record a standard result for a child")
    @ResponseStatus(HttpStatus.CREATED)
    public ChildStandardResponse create(@PathVariable UUID childId,
                                        @Valid @RequestBody CreateChildStandardRequest request) {
        return childStandardService.create(childId, request);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all results", description = "Get all standard results for a child")
    public List<ChildStandardResponse> getAll(@PathVariable UUID childId) {
        return childStandardService.getByChild(childId);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Update result", description = "Update a standard result")
    public ChildStandardResponse update(@PathVariable UUID childId, @PathVariable UUID id,
                                        @Valid @RequestBody CreateChildStandardRequest request) {
        return childStandardService.update(childId, id, request);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Delete result", description = "Remove a standard result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID childId, @PathVariable UUID id) {
        childStandardService.delete(childId, id);
    }
}