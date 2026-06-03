package com.par.jbfh.training.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.repository.ExerciseRepository;
import com.par.jbfh.team.entity.Team;
import com.par.jbfh.team.repository.TeamRepository;
import com.par.jbfh.training.dto.AddExerciseToTrainingRequest;
import com.par.jbfh.training.dto.CreateTemplateTrainingRequest;
import com.par.jbfh.training.dto.TemplateTrainingResponse;
import com.par.jbfh.training.dto.TrainingResponse;
import com.par.jbfh.training.entity.TemplateTraining;
import com.par.jbfh.training.entity.TemplateTrainingExercise;
import com.par.jbfh.training.entity.Training;
import com.par.jbfh.training.entity.TrainingExercise;
import com.par.jbfh.training.repository.TemplateTrainingRepository;
import com.par.jbfh.training.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateTrainingService {

    private final TemplateTrainingRepository templateTrainingRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

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
        templateTrainingRepository.delete(template);
        log.info("Deleted template training '{}'", template.getName());
    }

    @Transactional
    public TrainingResponse apply(UUID templateId, UUID teamId) {
        User currentUser = getCurrentUser();
        TemplateTraining template = templateTrainingRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + templateId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        Training training = new Training();
        training.setName(template.getName());
        training.setDescription(template.getDescription());
        training.setTask1(template.getTask1());
        training.setTask2(template.getTask2());
        training.setTask3(template.getTask3());
        training.setTeam(team);
        training.setSourceTemplate(template);
        training.setCreatedBy(currentUser);
        training.setDate(java.time.LocalDate.now());

        training = trainingRepository.save(training);
        log.info("Applied template '{}' to team '{}' — created training '{}'",
                template.getName(), team.getName(), training.getName());
        return toTrainingResponse(training);
    }

    @Transactional
    public void addExercise(UUID templateId, AddExerciseToTrainingRequest request) {
        TemplateTraining template = templateTrainingRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + templateId));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + request.getExerciseId()));

        TemplateTrainingExercise te = new TemplateTrainingExercise();
        te.setTemplateTraining(template);
        te.setExercise(exercise);
        te.setWorkDuration(request.getWorkDuration());
        te.setIntensity(request.getIntensity());
        te.setExplanationDuration(request.getExplanationDuration());
        te.setLoadLevel(request.getLoadLevel());
        // calculate() вызывается автоматически через @PrePersist
    }

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

    private TrainingResponse toTrainingResponse(Training training) {
        return new TrainingResponse(
                training.getId(),
                training.getName(),
                training.getDate(),
                training.getTimeStart(),
                training.getTimeEnd(),
                training.getLocation() != null ? training.getLocation().getId() : null,
                training.getLocation() != null ? training.getLocation().getName() : null,
                training.getTeam().getId(),
                training.getTeam().getName(),
                training.getPicturePath() != null
                        ? "/api/v1/trainings/" + training.getId() + "/picture"
                        : null,
                training.getDescription(),
                training.getTask1(),
                training.getTask2(),
                training.getTask3(),
                training.getSourceTemplate() != null ? training.getSourceTemplate().getId() : null,
                training.getSourceTemplate() != null ? training.getSourceTemplate().getName() : null,
                training.getCreatedBy().getId(),
                training.getCreatedBy().getUsername(),
                training.getCreatedAt(),
                training.getUpdatedAt()
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUserId()));
    }
}