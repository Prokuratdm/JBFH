package com.par.jbfh.exercise.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.exercise.dto.CreateExerciseRequest;
import com.par.jbfh.exercise.dto.ExerciseResponse;
import com.par.jbfh.exercise.dto.UpdateExerciseRequest;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.entity.ExerciseInventory;
import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.exercise.repository.ExerciseInventoryRepository;
import com.par.jbfh.exercise.repository.ExerciseRepository;
import com.par.jbfh.inventory.entity.Inventory;
import com.par.jbfh.inventory.repository.InventoryRepository;
import com.par.jbfh.storage.FileStorage;
import com.par.jbfh.storage.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseInventoryRepository exerciseInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    @Transactional
    public ExerciseResponse create(CreateExerciseRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdminOrMethodist = hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_METHODIST");

        if (exerciseRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Exercise already exists with name: " + request.getName());
        }

        Exercise exercise = new Exercise();
        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setType(request.getType());
        exercise.setActive(true);

        if (isAdminOrMethodist) {
            if (request.getClubId() != null) {
                Club club = clubRepository.findById(request.getClubId())
                        .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
                exercise.setClub(club);
            }
        } else {
            if (currentUser.getClub() == null) {
                throw new IllegalArgumentException("Club is required for non-admin users");
            }
            exercise.setClub(currentUser.getClub());
        }

        exercise = exerciseRepository.save(exercise);

        if (request.getInventoryIds() != null && !request.getInventoryIds().isEmpty()) {
            for (UUID inventoryId : request.getInventoryIds()) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));
                exerciseInventoryRepository.save(new ExerciseInventory(exercise, inventory));
            }
        }

        log.info("Created exercise '{}' (type={})", exercise.getName(), exercise.getType());
        return toExerciseResponse(exercise);
    }

    @Transactional(readOnly = true)
    public Page<ExerciseResponse> getAll(boolean activeOnly, ExerciseType type, Pageable pageable) {
        User currentUser = getCurrentUser();
        boolean isAdminOrMethodist = hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_METHODIST");

        Specification<Exercise> spec = (root, query, cb) -> cb.conjunction();

        if (activeOnly) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("active")));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        if (!isAdminOrMethodist) {
            UUID clubId = currentUser.getClub() != null ? currentUser.getClub().getId() : null;
            if (clubId == null) {
                return Page.empty(pageable);
            }
            UUID finalClubId = clubId;
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.isNull(root.get("club").get("id")),
                    cb.equal(root.get("club").get("id"), finalClubId)
            ));
        }

        return exerciseRepository.findAll(spec, pageable).map(this::toExerciseResponse);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));
        return toExerciseResponse(exercise);
    }

    @Transactional
    public ExerciseResponse update(UUID id, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));

        if (request.getName() != null) {
            if (exerciseRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new IllegalArgumentException("Exercise already exists with name: " + request.getName());
            }
            exercise.setName(request.getName());
        }
        if (request.getDescription() != null) {
            exercise.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            exercise.setType(request.getType());
        }
        if (request.getInventoryIds() != null) {
            exerciseInventoryRepository.deleteByExerciseId(id);
            for (UUID inventoryId : request.getInventoryIds()) {
                Inventory inventory = inventoryRepository.findById(inventoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));
                exerciseInventoryRepository.save(new ExerciseInventory(exercise, inventory));
            }
        }

        exercise = exerciseRepository.save(exercise);
        log.info("Updated exercise '{}'", exercise.getName());
        return toExerciseResponse(exercise);
    }

    @Transactional
    public ExerciseResponse setActive(UUID id, boolean active) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));

        exercise.setActive(active);
        exercise = exerciseRepository.save(exercise);
        log.info("Exercise '{}' active status set to {}", exercise.getName(), active);

        return toExerciseResponse(exercise);
    }

    @Transactional
    public ExerciseResponse uploadPicture(UUID id, MultipartFile file) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));

        if (exercise.getPicturePath() != null) {
            fileStorage.delete(exercise.getPicturePath());
        }

        String picturePath = fileStorage.save(file, exercise.getId(), FileType.EXERCISE_PICTURE);
        exercise.setPicturePath(picturePath);
        exercise = exerciseRepository.save(exercise);

        log.info("Uploaded picture for exercise '{}'", exercise.getName());
        return toExerciseResponse(exercise);
    }

    public Resource getPicture(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + id));

        if (exercise.getPicturePath() == null) {
            throw new IllegalArgumentException("Exercise has no picture: " + id);
        }
        return fileStorage.getResource(exercise.getPicturePath());
    }

    public List<String> getTypes() {
        return Arrays.stream(ExerciseType.values())
                .map(ExerciseType::name)
                .toList();
    }

    private ExerciseResponse toExerciseResponse(Exercise exercise) {
        List<UUID> inventoryIds = exerciseInventoryRepository.findByExerciseId(exercise.getId()).stream()
                .map(ei -> ei.getInventory().getId())
                .toList();

        String pictureUrl = exercise.getPicturePath() != null
                ? "/api/v1/exercises/" + exercise.getId() + "/picture"
                : null;

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getType(),
                pictureUrl,
                exercise.isActive(),
                exercise.getClub() != null ? exercise.getClub().getId() : null,
                exercise.getClub() != null ? exercise.getClub().getName() : null,
                inventoryIds,
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
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

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
    }
}