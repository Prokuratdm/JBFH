package com.par.jbfh.training.service;

import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.location.entity.Location;
import com.par.jbfh.location.repository.LocationRepository;
import com.par.jbfh.team.entity.Team;
import com.par.jbfh.team.repository.TeamRepository;
import com.par.jbfh.training.dto.CreateTrainingRequest;
import com.par.jbfh.training.dto.TrainingResponse;
import com.par.jbfh.training.dto.UpdateTrainingRequest;
import com.par.jbfh.training.entity.TemplateTraining;
import com.par.jbfh.training.entity.Training;
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

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TeamRepository teamRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TemplateTrainingRepository templateTrainingRepository;

    @Transactional
    public TrainingResponse create(UUID teamId, CreateTrainingRequest request) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + request.getLocationId()));

        Training training = new Training();
        training.setName(request.getName());
        training.setDate(request.getDate());
        training.setTimeStart(request.getTimeStart());
        training.setTimeEnd(request.getTimeEnd());
        training.setLocation(location);
        training.setTeam(team);
        training.setDescription(request.getDescription());
        training.setTask1(request.getTask1());
        training.setTask2(request.getTask2());
        training.setTask3(request.getTask3());
        training.setCreatedBy(currentUser);

        if (request.getSourceTemplateId() != null) {
            TemplateTraining template = templateTrainingRepository.findById(request.getSourceTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template training not found: " + request.getSourceTemplateId()));
            training.setSourceTemplate(template);
        }

        training = trainingRepository.save(training);
        log.info("Created training '{}' for team '{}' on {}", training.getName(), team.getName(), training.getDate());
        return toResponse(training);
    }

    @Transactional(readOnly = true)
    public Page<TrainingResponse> getByTeam(UUID teamId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Page<Training> page;
        if (dateFrom != null && dateTo != null) {
            page = trainingRepository.findByTeamIdAndDateBetween(teamId, dateFrom, dateTo, pageable);
        } else {
            page = trainingRepository.findByTeamId(teamId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TrainingResponse getById(UUID teamId, UUID id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found: " + id));
        if (!training.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Training does not belong to team: " + teamId);
        }
        return toResponse(training);
    }

    @Transactional
    public TrainingResponse update(UUID teamId, UUID id, UpdateTrainingRequest request) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found: " + id));
        if (!training.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Training does not belong to team: " + teamId);
        }

        if (request.getName() != null) training.setName(request.getName());
        if (request.getDate() != null) training.setDate(request.getDate());
        if (request.getTimeStart() != null) training.setTimeStart(request.getTimeStart());
        if (request.getTimeEnd() != null) training.setTimeEnd(request.getTimeEnd());
        if (request.getDescription() != null) training.setDescription(request.getDescription());
        if (request.getTask1() != null) training.setTask1(request.getTask1());
        if (request.getTask2() != null) training.setTask2(request.getTask2());
        if (request.getTask3() != null) training.setTask3(request.getTask3());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Location not found: " + request.getLocationId()));
            training.setLocation(location);
        }

        training = trainingRepository.save(training);
        log.info("Updated training '{}'", training.getName());
        return toResponse(training);
    }

    @Transactional
    public void delete(UUID teamId, UUID id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found: " + id));
        if (!training.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Training does not belong to team: " + teamId);
        }
        trainingRepository.delete(training);
        log.info("Deleted training '{}'", training.getName());
    }

    private TrainingResponse toResponse(Training training) {
        return new TrainingResponse(
                training.getId(),
                training.getName(),
                training.getDate(),
                training.getTimeStart(),
                training.getTimeEnd(),
                training.getLocation().getId(),
                training.getLocation().getName(),
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