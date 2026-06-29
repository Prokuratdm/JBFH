package com.par.jbfh.training.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.repository.ExerciseRepository;
import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateSetRequest;
import com.par.jbfh.training.dto.SetResponse;
import com.par.jbfh.training.dto.TrainingExerciseResponse;
import com.par.jbfh.training.entity.Set;
import com.par.jbfh.training.entity.SetExercise;
import com.par.jbfh.training.repository.SetExerciseRepository;
import com.par.jbfh.training.repository.SetRepository;
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
public class SetService {

    private final SetRepository setRepository;
    private final SetExerciseRepository setExerciseRepository;
    private final ClubRepository clubRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseCalculator exerciseCalculator;

    @Transactional
    public SetResponse create(CreateSetRequest request) {
        if (setRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Set already exists with name: " + request.getName());
        }

        Set set = new Set();
        set.setName(request.getName());
        set.setTrainingPart(request.getTrainingPart());

        if (request.getClubId() != null) {
            Club club = clubRepository.findById(request.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
            set.setClub(club);
        }

        set = setRepository.save(set);
        log.info("Created set '{}'", set.getName());
        return toSetResponse(set);
    }

    @Transactional(readOnly = true)
    public Page<SetResponse> getAll(Pageable pageable) {
        return setRepository.findAll(pageable).map(this::toSetResponse);
    }

    @Transactional(readOnly = true)
    public SetResponse getById(UUID id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found: " + id));
        return toSetResponse(set);
    }

    @Transactional
    public SetResponse update(UUID id, CreateSetRequest request) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found: " + id));

        if (request.getName() != null) set.setName(request.getName());
        if (request.getTrainingPart() != null) set.setTrainingPart(request.getTrainingPart());

        set = setRepository.save(set);
        log.info("Updated set '{}'", set.getName());
        return toSetResponse(set);
    }

    @Transactional
    public void delete(UUID id) {
        Set set = setRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Set not found: " + id));
        setExerciseRepository.deleteBySetId(id);
        setRepository.delete(set);
        log.info("Deleted set '{}'", set.getName());
    }

    @Transactional
    public TrainingExerciseResponse addExercise(UUID setId, AddExerciseToTrainingRequest request) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Set not found: " + setId));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + request.getExerciseId()));

        SetExercise se = new SetExercise();
        se.setSet(set);
        se.setExercise(exercise);
        se.setWorkDuration(request.getWorkDuration());
        se.setIntensity(request.getIntensity());
        se.setExplanationDuration(request.getExplanationDuration());
        se.setLoadLevel(request.getLoadLevel());
        se.setRepetitions(request.getRepetitions());

        var calc = exerciseCalculator.calculateRestAndMode(se.getWorkDuration(), se.getIntensity());
        se.setRestDuration(calc.restDuration());
        se.setWorkMode(calc.workMode());
        se.setTotalTime(exerciseCalculator.calculateTotalTime(
                se.getWorkDuration(), se.getRestDuration(),
                se.getRepetitions(), se.getExplanationDuration()));

        se = setExerciseRepository.save(se);
        return toExerciseResponse(se);
    }

    @Transactional(readOnly = true)
    public List<TrainingExerciseResponse> getExercises(UUID setId) {
        return setExerciseRepository.findBySetId(setId).stream()
                .map(this::toExerciseResponse)
                .toList();
    }

    @Transactional
    public void deleteExercise(UUID setId, UUID exerciseId) {
        SetExercise se = setExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("SetExercise not found: " + exerciseId));
        if (!se.getSet().getId().equals(setId)) {
            throw new IllegalArgumentException("Exercise does not belong to set: " + setId);
        }
        setExerciseRepository.delete(se);
    }

    private SetResponse toSetResponse(Set set) {
        return new SetResponse(
                set.getId(),
                set.getName(),
                set.getTrainingPart(),
                set.getClub() != null ? set.getClub().getId() : null,
                set.getClub() != null ? set.getClub().getName() : null,
                set.getCreatedAt(),
                set.getUpdatedAt()
        );
    }

    private TrainingExerciseResponse toExerciseResponse(SetExercise se) {
        return new TrainingExerciseResponse(
                se.getId(),
                se.getSet().getId(),
                se.getExercise().getId(),
                se.getExercise().getName(),
                se.getWorkDuration(),
                se.getIntensity(),
                se.getRestDuration(),
                se.getExplanationDuration(),
                se.getWorkMode(),
                se.getTotalTime(),
                se.getLoadLevel(),
                se.getRepetitions()
        );
    }
}