package com.par.jbfh.team.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.storage.FileStorage;
import com.par.jbfh.storage.enums.FileType;
import com.par.jbfh.team.dto.*;
import com.par.jbfh.team.entity.Team;
import com.par.jbfh.team.entity.TeamCoach;
import com.par.jbfh.team.repository.TeamCoachRepository;
import com.par.jbfh.team.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamCoachRepository teamCoachRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private TeamService teamService;

    private UUID clubId;
    private UUID userId;
    private Club club;
    private User currentUser;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, teamCoachRepository, clubRepository, userRepository, fileStorage);

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

    // region createTeam

    @Test
    void createTeamShouldSucceed() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");
        request.setYear(2015);
        request.setDescription("Description");

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(teamRepository.existsByNameAndClubId("Team A", clubId)).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });
        when(teamCoachRepository.findByTeamId(any())).thenReturn(List.of());

        TeamResponse response = teamService.createTeam(clubId, request);

        assertThat(response.name()).isEqualTo("Team A");
        assertThat(response.year()).isEqualTo(2015);
        assertThat(response.description()).isEqualTo("Description");
        assertThat(response.active()).isTrue();
        assertThat(response.clubId()).isEqualTo(clubId);
    }

    @Test
    void createTeamShouldThrowWhenClubNotFound() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");
        request.setYear(2015);

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(clubId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + clubId);
    }

    @Test
    void createTeamShouldThrowWhenNameExistsInClub() {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team A");
        request.setYear(2015);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(teamRepository.existsByNameAndClubId("Team A", clubId)).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam(clubId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team already exists with name 'Team A' in this club");
    }

    // endregion

    // region getTeamsByClub

    @Test
    void getTeamsByClubShouldReturnPageForClubUser() {
        mockAuthenticatedUser(currentUser);

        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setActive(true);
        Page<Team> page = new PageImpl<>(List.of(team));

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(teamRepository.findByClubIdAndActiveTrue(eq(clubId), any(Pageable.class))).thenReturn(page);
        when(teamCoachRepository.findByTeamId(any())).thenReturn(List.of());

        Page<TeamResponse> result = teamService.getTeamsByClub(clubId, PageRequest.of(0, 18));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Team A");
    }

    @Test
    void getTeamsByClubShouldThrowWhenClubUserAccessesOtherClub() {
        Club otherClub = new Club("Other Club");
        otherClub.setId(UUID.randomUUID());
        currentUser.setClub(otherClub);
        mockAuthenticatedUser(currentUser);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

        assertThatThrownBy(() -> teamService.getTeamsByClub(clubId, PageRequest.of(0, 18)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access denied: you can only view teams of your own club");
    }

    @Test
    void getTeamsByClubShouldReturnAllForClubMethodist() {
        currentUser.setRoles(Set.of(new Role("ROLE_CLUB_METHODIST")));
        currentUser.setClub(null);
        mockAuthenticatedUser(currentUser);

        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setActive(true);
        Page<Team> page = new PageImpl<>(List.of(team));

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(teamRepository.findByClubIdAndActiveTrue(eq(clubId), any(Pageable.class))).thenReturn(page);
        when(teamCoachRepository.findByTeamId(any())).thenReturn(List.of());

        Page<TeamResponse> result = teamService.getTeamsByClub(clubId, PageRequest.of(0, 18));

        assertThat(result.getContent()).hasSize(1);
    }

    // endregion

    // region getTeamById

    @Test
    void getTeamByIdShouldReturnWhenFound() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setActive(true);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.getTeamById(clubId, teamId);

        assertThat(response.name()).isEqualTo("Team A");
    }

    @Test
    void getTeamByIdShouldThrowWhenNotFound() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(clubId, teamId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team not found: " + teamId + " in club: " + clubId);
    }

    // endregion

    // region updateTeam

    @Test
    void updateTeamShouldUpdateFields() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Old Name");
        team.setYear(2015);
        team.setDescription("Old desc");
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("New Name");
        request.setYear(2016);
        request.setDescription("New desc");

        TeamResponse response = teamService.updateTeam(clubId, teamId, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.year()).isEqualTo(2016);
        assertThat(response.description()).isEqualTo("New desc");
    }

    @Test
    void updateTeamShouldThrowWhenNameExists() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Old Name");
        team.setYear(2015);
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamRepository.existsByNameAndClubIdAndIdNot("New Name", clubId, teamId)).thenReturn(true);

        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("New Name");

        assertThatThrownBy(() -> teamService.updateTeam(clubId, teamId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team already exists with name 'New Name' in this club");
    }

    @Test
    void updateTeamShouldThrowWhenNotFound() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.updateTeam(clubId, teamId, new UpdateTeamRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team not found: " + teamId + " in club: " + clubId);
    }

    // endregion

    // region setActive

    @Test
    void setActiveShouldDeactivateTeam() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setActive(true);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.setActive(clubId, teamId, false);

        assertThat(response.active()).isFalse();
    }

    @Test
    void setActiveShouldActivateTeam() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setActive(false);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.setActive(clubId, teamId, true);

        assertThat(response.active()).isTrue();
    }

    // endregion

    // region uploadLogo

    @Test
    void uploadLogoShouldSaveAndReturnUpdated() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        MultipartFile file = mock(MultipartFile.class);
        when(fileStorage.save(file, teamId, FileType.TEAM_LOGO)).thenReturn("uploads/logos/teams/logo.png");
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.uploadLogo(clubId, teamId, file);

        assertThat(response.logoUrl()).isEqualTo("/api/v1/clubs/" + clubId + "/teams/" + teamId + "/logo");
    }

    @Test
    void uploadLogoShouldDeleteOldLogoWhenExists() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setLogoPath("old/path.png");

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        MultipartFile file = mock(MultipartFile.class);
        when(fileStorage.save(file, teamId, FileType.TEAM_LOGO)).thenReturn("new/path.png");
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        teamService.uploadLogo(clubId, teamId, file);

        verify(fileStorage).delete("old/path.png");
    }

    @Test
    void uploadLogoShouldThrowWhenTeamNotFound() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.uploadLogo(clubId, teamId, mock(MultipartFile.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team not found: " + teamId + " in club: " + clubId);
    }

    // endregion

    // region getLogo

    @Test
    void getLogoShouldReturnResource() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);
        team.setLogoPath("logos/teams/test.png");

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        Resource resource = new ByteArrayResource("test".getBytes());
        when(fileStorage.getResource("logos/teams/test.png")).thenReturn(resource);

        Resource result = teamService.getLogo(clubId, teamId);

        assertThat(result).isNotNull();
    }

    @Test
    void getLogoShouldThrowWhenNoLogo() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.getLogo(clubId, teamId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team has no logo: " + teamId);
    }

    @Test
    void getLogoShouldThrowWhenTeamNotFound() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getLogo(clubId, teamId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team not found: " + teamId + " in club: " + clubId);
    }

    // endregion

    // region assignCoach

    @Test
    void assignCoachShouldSucceed() {
        UUID teamId = UUID.randomUUID();
        UUID coachUserId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        User coachUser = new User();
        coachUser.setId(coachUserId);
        coachUser.setUsername("coach1");

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(userRepository.findById(coachUserId)).thenReturn(Optional.of(coachUser));
        when(teamCoachRepository.existsByTeamIdAndUserId(teamId, coachUserId)).thenReturn(false);
        when(teamCoachRepository.save(any(TeamCoach.class))).thenReturn(new TeamCoach(team, coachUser));
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.assignCoach(clubId, teamId, coachUserId);

        assertThat(response).isNotNull();
        verify(teamCoachRepository).save(any(TeamCoach.class));
    }

    @Test
    void assignCoachShouldThrowWhenTeamNotFound() {
        UUID teamId = UUID.randomUUID();
        UUID coachUserId = UUID.randomUUID();
        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.assignCoach(clubId, teamId, coachUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team not found: " + teamId + " in club: " + clubId);
    }

    @Test
    void assignCoachShouldThrowWhenAlreadyAssigned() {
        UUID teamId = UUID.randomUUID();
        UUID coachUserId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        User coachUser = new User();
        coachUser.setId(coachUserId);
        coachUser.setUsername("coach1");

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(userRepository.findById(coachUserId)).thenReturn(Optional.of(coachUser));
        when(teamCoachRepository.existsByTeamIdAndUserId(teamId, coachUserId)).thenReturn(true);

        assertThatThrownBy(() -> teamService.assignCoach(clubId, teamId, coachUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is already assigned as coach to this team");
    }

    // endregion

    // region removeCoach

    @Test
    void removeCoachShouldSucceed() {
        UUID teamId = UUID.randomUUID();
        UUID coachUserId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamCoachRepository.existsByTeamIdAndUserId(teamId, coachUserId)).thenReturn(true);
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of());
        doNothing().when(teamCoachRepository).deleteByTeamIdAndUserId(teamId, coachUserId);

        TeamResponse response = teamService.removeCoach(clubId, teamId, coachUserId);

        assertThat(response).isNotNull();
        verify(teamCoachRepository).deleteByTeamIdAndUserId(teamId, coachUserId);
    }

    @Test
    void removeCoachShouldThrowWhenNotAssigned() {
        UUID teamId = UUID.randomUUID();
        UUID coachUserId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamCoachRepository.existsByTeamIdAndUserId(teamId, coachUserId)).thenReturn(false);

        assertThatThrownBy(() -> teamService.removeCoach(clubId, teamId, coachUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is not assigned as coach to this team");
    }

    // endregion

    // region getCoaches

    @Test
    void getCoachesShouldReturnList() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setName("Team A");
        team.setYear(2015);
        team.setClub(club);

        User coach1 = new User();
        coach1.setId(UUID.randomUUID());
        coach1.setUsername("coach1");
        coach1.setEmail("coach1@test.com");

        User coach2 = new User();
        coach2.setId(UUID.randomUUID());
        coach2.setUsername("coach2");
        coach2.setEmail("coach2@test.com");

        when(teamRepository.findByIdAndClubId(teamId, clubId)).thenReturn(Optional.of(team));
        when(teamCoachRepository.findByTeamId(teamId)).thenReturn(List.of(
                new TeamCoach(team, coach1),
                new TeamCoach(team, coach2)
        ));

        List<CoachResponse> coaches = teamService.getCoaches(clubId, teamId);

        assertThat(coaches).hasSize(2);
        assertThat(coaches.get(0).username()).isEqualTo("coach1");
        assertThat(coaches.get(1).username()).isEqualTo("coach2");
    }

    // endregion
}