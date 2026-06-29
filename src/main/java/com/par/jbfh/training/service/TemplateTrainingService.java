package com.par.jbfh.training.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.repository.ExerciseRepository;
import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateTemplateTrainingRequest;
import com.par.jbfh.training.dto.TemplateTrainingResponse;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.entity.TemplateTraining;
import com.par.jbfh.training.entity.TemplateTrainingExercise;
import com.par.jbfh.training.repository.TemplateTrainingExerciseRepository;
import com.par.jbfh.training.repository.TemplateTrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateTrainingService {

    private final TemplateTrainingRepository templateTrainingRepository;
    private final TemplateTrainingExerciseRepository templateTrainingExerciseRepository;
    private final ClubRepository clubRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseCalculator exerciseCalculator;

    @Transactional
    public TemplateTrainingResponse create(CreateTemplateTrainingRequest request) {
        TemplateTraining template = new TemplateTraining();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTask1(request.getTask1());
        template.setTask2(request.getTask2());
        template.setTask3(request.getTask3());

        if (request.getClubId() != null) {
            Club club = clubRepository.findById(request.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
            template.setClub(club);
        }

        template = templateTrainingRepository.save(template);
        log.info("Created template training '{}'", template.getName());
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<TemplateTrainingResponse> getAll(Pageable pageable) {
        return templateTrainingRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TemplateTrainingResponse getById(UUID id) {
        TemplateTraining template = templateTrainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + id));
        return toResponse(template);
    }

    @Transactional
    public TemplateTrainingResponse update(UUID id, CreateTemplateTrainingRequest request) {
        TemplateTraining template = templateTrainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + id));

        if (request.getName() != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getTask1() != null) template.setTask1(request.getTask1());
        if (request.getTask2() != null) template.setTask2(request.getTask2());
        if (request.getTask3() != null) template.setTask3(request.getTask3());

        template = templateTrainingRepository.save(template);
        log.info("Updated template training '{}'", template.getName());
        return toResponse(template);
    }

    @Transactional
    public void delete(UUID id) {
        TemplateTraining template = templateTrainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + id));
        templateTrainingExerciseRepository.deleteByTemplateTrainingId(id);
        templateTrainingRepository.delete(template);
        log.info("Deleted template training '{}'", template.getName());
    }

    // region Exercises

    @Transactional
    public TrainingExerciseResponse addExercise(UUID templateTrainingId, AddExerciseToTrainingRequest request) {
        TemplateTraining template = templateTrainingRepository.findById(templateTrainingId)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + templateTrainingId));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + request.getExerciseId()));

        TemplateTrainingExercise te = new TemplateTrainingExercise();
        te.setTemplateTraining(template);
        te.setExercise(exercise);
        te.setWorkDuration(request.getWorkDuration());
        te.setIntensity(request.getIntensity());
        te.setExplanationDuration(request.getExplanationDuration());
        te.setLoadLevel(request.getLoadLevel());
        te.setRepetitions(request.getRepetitions());

        var calc = exerciseCalculator.calculateRestAndMode(te.getWorkDuration(), te.getIntensity());
        te.setRestDuration(calc.restDuration());
        te.setWorkMode(calc.workMode());
        te.setTotalTime(exerciseCalculator.calculateTotalTime(
                te.getWorkDuration(), te.getRestDuration(),
                te.getRepetitions(), te.getExplanationDuration()));

        te = templateTrainingExerciseRepository.save(te);
        return toExerciseResponse(te);
    }

    @Transactional(readOnly = true)
    public List<TrainingExerciseResponse> getExercises(UUID templateTrainingId) {
        return templateTrainingExerciseRepository.findByTemplateTrainingId(templateTrainingId).stream()
                .map(this::toExerciseResponse)
                .toList();
    }

    @Transactional
    public TrainingExerciseResponse updateExercise(UUID templateTrainingId, UUID exerciseId,
                                                    AddExerciseToTrainingRequest request) {
        TemplateTrainingExercise te = templateTrainingExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("TemplateTrainingExercise not found: " + exerciseId));
        if (!te.getTemplateTraining().getId().equals(templateTrainingId)) {
            throw new IllegalArgumentException("Exercise does not belong to template: " + templateTrainingId);
        }

        te.setWorkDuration(request.getWorkDuration());
        te.setIntensity(request.getIntensity());
        te.setExplanationDuration(request.getExplanationDuration());
        te.setLoadLevel(request.getLoadLevel());
        te.setRepetitions(request.getRepetitions());

        var calc = exerciseCalculator.calculateRestAndMode(te.getWorkDuration(), te.getIntensity());
        te.setRestDuration(calc.restDuration());
        te.setWorkMode(calc.workMode());
        te.setTotalTime(exerciseCalculator.calculateTotalTime(
                te.getWorkDuration(), te.getRestDuration(),
                te.getRepetitions(), te.getExplanationDuration()));

        te = templateTrainingExerciseRepository.save(te);
        return toExerciseResponse(te);
    }

    @Transactional
    public void deleteExercise(UUID templateTrainingId, UUID exerciseId) {
        TemplateTrainingExercise te = templateTrainingExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("TemplateTrainingExercise not found: " + exerciseId));
        if (!te.getTemplateTraining().getId().equals(templateTrainingId)) {
            throw new IllegalArgumentException("Exercise does not belong to template: " + templateTrainingId);
        }
        templateTrainingExerciseRepository.delete(te);
    }

    // endregion

    private TemplateTrainingResponse toResponse(TemplateTraining template) {
        return new TemplateTrainingResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getPicturePath() != null
                        ? "/api/v1/template-trainings/" + template.getId() + "/picture"
                        : null,
                template.getTask1(),
                template.getTask2(),
                template.getTask3(),
                template.getClub() != null ? template.getClub().getId() : null,
                template.getClub() != null ? template.getClub().getName() : null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private TrainingExerciseResponse toExerciseResponse(TemplateTrainingExercise te) {
        return new TrainingExerciseResponse(
                te.getId(),
                te.getTemplateTraining().getId(),
                te.getExercise().getId(),
                te.getExercise().getName(),
                te.getWorkDuration(),
                te.getIntensity(),
                te.getRestDuration(),
                te.getExplanationDuration(),
                te.getWorkMode(),
                te.getTotalTime(),
                te.getLoadLevel(),
                te.getRepetitions()
        );
    }
}