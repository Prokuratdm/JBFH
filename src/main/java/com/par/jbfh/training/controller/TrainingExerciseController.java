package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.entity.Training;
import com.par.jbfh.training.entity.TrainingExercise;
import com.par.jbfh.training.repository.TrainingExerciseRepository;
import com.par.jbfh.training.repository.TrainingRepository;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.repository.ExerciseRepository;
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
@RequestMapping("/api/v1/trainings/{trainingId}/exercises")
@RequiredArgsConstructor
@Tag(name = "Training Exercises", description = "Exercises within a training")
public class TrainingExerciseController {

    private final TrainingExerciseRepository trainingExerciseRepository;
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

    @PostMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Add exercise", description = "Add an exercise to a training with parameters")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingExerciseResponse addExercise(@PathVariable UUID trainingId,
                                                 @Valid @RequestBody AddExerciseToTrainingRequest request) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new IllegalArgumentException("Training not found: " + trainingId));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + request.getExerciseId()));

        TrainingExercise te = new TrainingExercise();
        te.setTraining(training);
        te.setExercise(exercise);
        te.setWorkDuration(request.getWorkDuration());
        te.setIntensity(request.getIntensity());
        te.setExplanationDuration(request.getExplanationDuration());
        te.setLoadLevel(request.getLoadLevel());

        te = trainingExerciseRepository.save(te);
        return toResponse(te);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "List exercises", description = "Get all exercises for a training")
    public List<TrainingExerciseResponse> getAll(@PathVariable UUID trainingId) {
        return trainingExerciseRepository.findByTrainingId(trainingId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Update exercise params", description = "Update exercise parameters in training")
    public TrainingExerciseResponse update(@PathVariable UUID trainingId, @PathVariable UUID id,
                                            @Valid @RequestBody AddExerciseToTrainingRequest request) {
        TrainingExercise te = trainingExerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TrainingExercise not found: " + id));
        if (!te.getTraining().getId().equals(trainingId)) {
            throw new IllegalArgumentException("Exercise does not belong to training: " + trainingId);
        }

        te.setWorkDuration(request.getWorkDuration());
        te.setIntensity(request.getIntensity());
        te.setExplanationDuration(request.getExplanationDuration());
        te.setLoadLevel(request.getLoadLevel());

        te = trainingExerciseRepository.save(te);
        return toResponse(te);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Remove exercise", description = "Remove an exercise from a training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID trainingId, @PathVariable UUID id) {
        TrainingExercise te = trainingExerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TrainingExercise not found: " + id));
        if (!te.getTraining().getId().equals(trainingId)) {
            throw new IllegalArgumentException("Exercise does not belong to training: " + trainingId);
        }
        trainingExerciseRepository.delete(te);
    }

    private TrainingExerciseResponse toResponse(TrainingExercise te) {
        return new TrainingExerciseResponse(
                te.getId(),
                te.getTraining().getId(),
                te.getExercise().getId(),
                te.getExercise().getName(),
                te.getWorkDuration(),
                te.getIntensity(),
                te.getRestDuration(),
                te.getExplanationDuration(),
                te.getWorkMode(),
                te.getTotalTime(),
                te.getLoadLevel()
        );
    }
}