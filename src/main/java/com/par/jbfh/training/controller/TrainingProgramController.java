package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.CreateTrainingProgramRequest;
import com.par.jbfh.training.dto.TrainingProgramResponse;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;
import com.par.jbfh.training.service.TrainingProgramService;
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
@RequestMapping("/api/v1/training-programs")
@RequiredArgsConstructor
@Tag(name = "Training Programs", description = "Training programs management API")
public class TrainingProgramController {

    private final TrainingProgramService programService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Create training program record")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingProgramResponse create(@Valid @RequestBody CreateTrainingProgramRequest request) {
        return programService.create(request);
    }

    @GetMapping
    @Operation(summary = "List training programs", description = "Filter by birthYear, loadLevel, cycle")
    public List<TrainingProgramResponse> getAll(
            @RequestParam(required = false) Integer birthYear,
            @RequestParam(required = false) LoadLevel loadLevel,
            @RequestParam(required = false) TrainingCycle cycle) {
        return programService.getAll(birthYear, loadLevel, cycle);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get training program record")
    public TrainingProgramResponse getById(@PathVariable UUID id) {
        return programService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Update training program record")
    public TrainingProgramResponse update(@PathVariable UUID id,
                                          @Valid @RequestBody CreateTrainingProgramRequest request) {
        return programService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Delete training program record")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        programService.delete(id);
    }
}