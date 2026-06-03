package com.par.jbfh.child.controller;

import com.par.jbfh.child.dto.ChildResponse;
import com.par.jbfh.child.dto.CreateChildRequest;
import com.par.jbfh.child.dto.UpdateChildRequest;
import com.par.jbfh.child.service.ChildService;
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
@RequestMapping("/api/v1/teams/{teamId}/children")
@RequiredArgsConstructor
@Tag(name = "Children", description = "Children management API")
public class ChildController {

    private final ChildService childService;

    @PostMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Create child", description = "Add a child to a team")
    @ResponseStatus(HttpStatus.CREATED)
    public ChildResponse create(@PathVariable UUID teamId, @Valid @RequestBody CreateChildRequest request) {
        return childService.create(teamId, request);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List children", description = "Get children of a team with pagination")
    public Page<ChildResponse> getAll(@PathVariable UUID teamId,
                                      @PageableDefault(size = 20) Pageable pageable) {
        return childService.getByTeam(teamId, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get child", description = "Get child details")
    public ChildResponse getById(@PathVariable UUID teamId, @PathVariable UUID id) {
        return childService.getById(teamId, id);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Update child", description = "Update child details")
    public ChildResponse update(@PathVariable UUID teamId, @PathVariable UUID id,
                                @Valid @RequestBody UpdateChildRequest request) {
        return childService.update(teamId, id, request);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Delete child", description = "Remove a child from a team")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID teamId, @PathVariable UUID id) {
        childService.delete(teamId, id);
    }
}