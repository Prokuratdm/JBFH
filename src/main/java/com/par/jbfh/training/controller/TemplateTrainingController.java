package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateTemplateTrainingRequest;
import com.par.jbfh.training.dto.TemplateTrainingResponse;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.service.TemplateTrainingService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/template-trainings")
@RequiredArgsConstructor
@Tag(name = "Template Trainings", description = "Template trainings management API")
public class TemplateTrainingController {

    private final TemplateTrainingService templateTrainingService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Create template training")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateTrainingResponse create(@Valid @RequestBody CreateTemplateTrainingRequest request) {
        return templateTrainingService.create(request);
    }

    @GetMapping
    @Operation(summary = "List template trainings")
    public Page<TemplateTrainingResponse> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return templateTrainingService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template training")
    public TemplateTrainingResponse getById(@PathVariable UUID id) {
        return templateTrainingService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Update template training")
    public TemplateTrainingResponse update(@PathVariable UUID id,
                                            @Valid @RequestBody CreateTemplateTrainingRequest request) {
        return templateTrainingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Delete template training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        templateTrainingService.delete(id);
    }

    @PostMapping("/{templateTrainingId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Add exercise to template training")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingExerciseResponse addExercise(@PathVariable UUID templateTrainingId,
                                                 @Valid @RequestBody AddExerciseToTrainingRequest request) {
        return templateTrainingService.addExercise(templateTrainingId, request);
    }

    @GetMapping("/{templateTrainingId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List exercises in template training")
    public List<TrainingExerciseResponse> getExercises(@PathVariable UUID templateTrainingId) {
        return templateTrainingService.getExercises(templateTrainingId);
    }

    @PutMapping("/{templateTrainingId}/exercises/{exerciseId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Update exercise params in template training")
    public TrainingExerciseResponse updateExercise(@PathVariable UUID templateTrainingId,
                                                    @PathVariable UUID exerciseId,
                                                    @Valid @RequestBody AddExerciseToTrainingRequest request) {
        return templateTrainingService.updateExercise(templateTrainingId, exerciseId, request);
    }

    @DeleteMapping("/{templateTrainingId}/exercises/{exerciseId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Remove exercise from template training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable UUID templateTrainingId, @PathVariable UUID exerciseId) {
        templateTrainingService.deleteExercise(templateTrainingId, exerciseId);
    }
}