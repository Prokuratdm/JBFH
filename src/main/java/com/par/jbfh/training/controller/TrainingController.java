package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateTrainingRequest;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.dto.TrainingResponse;
import com.par.jbfh.training.dto.UpdateTrainingRequest;
import com.par.jbfh.training.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/trainings")
@RequiredArgsConstructor
@Tag(name = "Trainings", description = "Trainings management API")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Create training")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingResponse create(@PathVariable UUID teamId,
                                   @Valid @RequestBody CreateTrainingRequest request) {
        return trainingService.create(teamId, request);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List trainings", description = "Filter by date range (dateFrom, dateTo)")
    public Page<TrainingResponse> getAll(@PathVariable UUID teamId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                         @PageableDefault(size = 10) Pageable pageable) {
        return trainingService.getByTeam(teamId, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get training")
    public TrainingResponse getById(@PathVariable UUID teamId, @PathVariable UUID id) {
        return trainingService.getById(teamId, id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Update training")
    public TrainingResponse update(@PathVariable UUID teamId, @PathVariable UUID id,
                                   @Valid @RequestBody UpdateTrainingRequest request) {
        return trainingService.update(teamId, id, request);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Delete training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID teamId, @PathVariable UUID id) {
        trainingService.delete(teamId, id);
    }

    @PostMapping("/{trainingId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Add exercise to training")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingExerciseResponse addExercise(@PathVariable UUID teamId,
                                                 @PathVariable UUID trainingId,
                                                 @Valid @RequestBody AddExerciseToTrainingRequest request) {
        return trainingService.addExercise(trainingId, request);
    }

    @GetMapping("/{trainingId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List exercises in training")
    public List<TrainingExerciseResponse> getExercises(@PathVariable UUID teamId,
                                                       @PathVariable UUID trainingId) {
        return trainingService.getExercises(trainingId);
    }

    @PutMapping("/{trainingId}/exercises/{exerciseId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Update exercise params in training")
    public TrainingExerciseResponse updateExercise(@PathVariable UUID teamId,
                                                    @PathVariable UUID trainingId,
                                                    @PathVariable UUID exerciseId,
                                                    @Valid @RequestBody AddExerciseToTrainingRequest request) {
        return trainingService.updateExercise(trainingId, exerciseId, request);
    }

    @DeleteMapping("/{trainingId}/exercises/{exerciseId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Remove exercise from training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable UUID teamId,
                               @PathVariable UUID trainingId,
                               @PathVariable UUID exerciseId) {
        trainingService.deleteExercise(trainingId, exerciseId);
    }
}
