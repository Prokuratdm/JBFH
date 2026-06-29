package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateSetRequest;
import com.par.jbfh.training.dto.SetResponse;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.service.SetService;
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
@RequestMapping("/api/v1/sets")
@RequiredArgsConstructor
@Tag(name = "Sets", description = "Training sets management API")
public class SetController {

    private final SetService setService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Create set", description = "Create a new training set (e.g., warm-up, cool-down).")
    @ResponseStatus(HttpStatus.CREATED)
    public SetResponse create(@Valid @RequestBody CreateSetRequest request) {
        return setService.create(request);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all sets", description = "Returns a paginated list of all sets.")
    public Page<SetResponse> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return setService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get set by ID")
    public SetResponse getById(@PathVariable UUID id) {
        return setService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Update set")
    public SetResponse update(@PathVariable UUID id, @Valid @RequestBody CreateSetRequest request) {
        return setService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Delete set")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        setService.delete(id);
    }

    @PostMapping("/{setId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Add exercise to set")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingExerciseResponse addExercise(@PathVariable UUID setId,
                                                 @Valid @RequestBody AddExerciseToTrainingRequest request) {
        return setService.addExercise(setId, request);
    }

    @GetMapping("/{setId}/exercises")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List exercises in set")
    public List<TrainingExerciseResponse> getExercises(@PathVariable UUID setId) {
        return setService.getExercises(setId);
    }

    @DeleteMapping("/{setId}/exercises/{exerciseId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Remove exercise from set")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable UUID setId, @PathVariable UUID exerciseId) {
        setService.deleteExercise(setId, exerciseId);
    }
}