package com.par.jbfh.exercise.controller;

import com.par.jbfh.exercise.dto.CreateExerciseRequest;
import com.par.jbfh.exercise.dto.ExerciseResponse;
import com.par.jbfh.exercise.dto.UpdateExerciseRequest;
import com.par.jbfh.exercise.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.par.jbfh.exercise.enums.ExerciseType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercises", description = "Exercises management API")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Create exercise", description = "Create a new exercise. Admin/methodist may omit clubId for global exercise.")
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse create(@Valid @RequestBody CreateExerciseRequest request) {
        return exerciseService.create(request);
    }

    @GetMapping("/types")
    @Operation(summary = "Get exercise types", description = "Returns list of available exercise types (ICE, LAND).")
    public List<String> getTypes() {
        return exerciseService.getTypes();
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all exercises", description = "Returns paginated list. Admins/methodists see all; others see global + their club's. Optional type filter.")
    public Page<ExerciseResponse> getAll(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) ExerciseType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return exerciseService.getAll(active, type, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get exercise by ID")
    public ExerciseResponse getById(@PathVariable UUID id) {
        return exerciseService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Update exercise")
    public ExerciseResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateExerciseRequest request) {
        return exerciseService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Activate or deactivate exercise")
    public ExerciseResponse setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return exerciseService.setActive(id, active);
    }

    @PostMapping("/{id}/picture")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Upload exercise picture", description = "Upload or update exercise picture. Max size 500KB. Image files only.")
    public ExerciseResponse uploadPicture(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return exerciseService.uploadPicture(id, file);
    }

    @GetMapping("/{id}/picture")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get exercise picture", description = "Returns the exercise picture image file.")
    public ResponseEntity<Resource> getPicture(@PathVariable UUID id) {
        Resource resource = exerciseService.getPicture(id);
        String contentType = "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}