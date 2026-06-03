package com.par.jbfh.exercise.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.exercise.dto.CreateExerciseRequest;
import com.par.jbfh.exercise.dto.ExerciseResponse;
import com.par.jbfh.exercise.dto.UpdateExerciseRequest;
import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.exercise.repository.ExerciseInventoryRepository;
import com.par.jbfh.exercise.repository.ExerciseRepository;
import com.par.jbfh.inventory.repository.InventoryRepository;
import com.par.jbfh.storage.FileStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock private ExerciseRepository exerciseRepository;
    @Mock private ExerciseInventoryRepository exerciseInventoryRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorage fileStorage;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private ExerciseService exerciseService;

    private UUID clubId;
    private UUID userId;
    private Club club;
    private User currentUser;

    @BeforeEach
    void setUp() {
        exerciseService = new ExerciseService(exerciseRepository, exerciseInventoryRepository,
                inventoryRepository, clubRepository, userRepository, fileStorage);

        clubId = UUID.randomUUID();
        userId = UUID.randomUUID();
        club = new Club("Test Club");
        club.setId(clubId);

        currentUser = new User();
        currentUser.setId(userId);
        currentUser.setUsername("testuser");
        currentUser.setClub(club);
        currentUser.setRoles(Set.of(new Role("ROLE_CLUB")));

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        UserPrincipal principal = new UserPrincipal(user.getUsername(), user.getId(), List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void createShouldSucceed() {
        mockAuthenticatedUser(currentUser);

        CreateExerciseRequest request = new CreateExerciseRequest();
        request.setName("Упражнение 1");
        request.setDescription("Описание");
        request.setType(ExerciseType.ICE);
        request.setUrl("https://example.com/video");
        request.setContent("Методика выполнения упражнения");

        when(exerciseRepository.existsByName("Упражнение 1")).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> {
            Exercise e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(exerciseInventoryRepository.findByExerciseId(any())).thenReturn(List.of());

        ExerciseResponse response = exerciseService.create(request);

        assertThat(response.name()).isEqualTo("Упражнение 1");
        assertThat(response.type()).isEqualTo(ExerciseType.ICE);
        assertThat(response.active()).isTrue();
        assertThat(response.clubId()).isEqualTo(clubId);
        assertThat(response.url()).isEqualTo("https://example.com/video");
        assertThat(response.content()).isEqualTo("Методика выполнения упражнения");
    }

    @Test
    void createShouldThrowWhenNameExists() {
        mockAuthenticatedUser(currentUser);

        CreateExerciseRequest request = new CreateExerciseRequest();
        request.setName("Duplicate");
        request.setType(ExerciseType.ICE);

        when(exerciseRepository.existsByName("Duplicate")).thenReturn(true);

        assertThatThrownBy(() -> exerciseService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exercise already exists with name: Duplicate");
    }

    @Test
    void getAllShouldReturnPageForAdmin() {
        currentUser.setRoles(Set.of(new Role("ROLE_ADMIN")));
        currentUser.setClub(null);
        mockAuthenticatedUser(currentUser);

        Exercise ex = new Exercise();
        ex.setId(UUID.randomUUID());
        ex.setName("Ex");
        ex.setType(ExerciseType.ICE);
        ex.setActive(true);
        Page<Exercise> page = new PageImpl<>(List.of(ex));

        when(exerciseRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        when(exerciseInventoryRepository.findByExerciseId(any())).thenReturn(List.of());

        Page<ExerciseResponse> result = exerciseService.getAll(true, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getByIdShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName("Ex");
        ex.setType(ExerciseType.LAND);
        ex.setActive(true);

        when(exerciseRepository.findById(id)).thenReturn(Optional.of(ex));
        when(exerciseInventoryRepository.findByExerciseId(id)).thenReturn(List.of());

        ExerciseResponse response = exerciseService.getById(id);

        assertThat(response.name()).isEqualTo("Ex");
        assertThat(response.type()).isEqualTo(ExerciseType.LAND);
    }

    @Test
    void updateShouldUpdateType() {
        UUID id = UUID.randomUUID();
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName("Old");
        ex.setType(ExerciseType.ICE);

        when(exerciseRepository.findById(id)).thenReturn(Optional.of(ex));
        when(exerciseRepository.save(any())).thenReturn(ex);
        when(exerciseInventoryRepository.findByExerciseId(id)).thenReturn(List.of());

        UpdateExerciseRequest request = new UpdateExerciseRequest();
        request.setType(ExerciseType.LAND);

        ExerciseResponse response = exerciseService.update(id, request);

        assertThat(response.type()).isEqualTo(ExerciseType.LAND);
    }

    @Test
    void setActiveShouldDeactivate() {
        UUID id = UUID.randomUUID();
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName("Ex");
        ex.setType(ExerciseType.ICE);
        ex.setActive(true);

        when(exerciseRepository.findById(id)).thenReturn(Optional.of(ex));
        when(exerciseRepository.save(any())).thenReturn(ex);
        when(exerciseInventoryRepository.findByExerciseId(id)).thenReturn(List.of());

        ExerciseResponse response = exerciseService.setActive(id, false);

        assertThat(response.active()).isFalse();
    }

    @Test
    void updateShouldUpdateUrlAndContent() {
        UUID id = UUID.randomUUID();
        Exercise ex = new Exercise();
        ex.setId(id);
        ex.setName("Old");
        ex.setType(ExerciseType.ICE);

        when(exerciseRepository.findById(id)).thenReturn(Optional.of(ex));
        when(exerciseRepository.save(any())).thenReturn(ex);
        when(exerciseInventoryRepository.findByExerciseId(id)).thenReturn(List.of());

        UpdateExerciseRequest request = new UpdateExerciseRequest();
        request.setUrl("https://example.com/new-video");
        request.setContent("Обновлённая методика");

        ExerciseResponse response = exerciseService.update(id, request);

        assertThat(response.url()).isEqualTo("https://example.com/new-video");
        assertThat(response.content()).isEqualTo("Обновлённая методика");
    }

    @Test
    void getTypesShouldReturnIceAndLand() {
        List<String> types = exerciseService.getTypes();

        assertThat(types).containsExactly("ICE", "LAND");
    }
}