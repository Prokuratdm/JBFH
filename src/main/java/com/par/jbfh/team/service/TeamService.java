package com.par.jbfh.team.service;

import com.par.jbfh.auth.entity.Club;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCoachRepository teamCoachRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    @Transactional
    public TeamResponse createTeam(UUID clubId, CreateTeamRequest request) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + clubId));

        if (teamRepository.existsByNameAndClubId(request.getName(), clubId)) {
            throw new IllegalArgumentException("Team already exists with name '" + request.getName() + "' in this club");
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setYear(request.getYear());
        team.setDescription(request.getDescription());
        team.setClub(club);
        team.setActive(true);
        team = teamRepository.save(team);

        log.info("Created team '{}' in club '{}'", team.getName(), club.getName());

        return toTeamResponse(team);
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeamsByClub(UUID clubId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + clubId));

        boolean isClubMethodist = hasRole(currentUser, "ROLE_CLUB_METHODIST");
        boolean isClubRole = hasRole(currentUser, "ROLE_CLUB");
        boolean isCoach = hasRole(currentUser, "ROLE_COACH") || hasRole(currentUser, "ROLE_MAIN_COACH");

        if (isClubMethodist) {
            // Club methodist sees all teams of all clubs
            return teamRepository.findByClubIdAndActiveTrue(clubId, pageable)
                    .map(this::toTeamResponse);
        }

        if (isClubRole) {
            // Club user sees only own club's teams
            if (currentUser.getClub() == null || !currentUser.getClub().getId().equals(clubId)) {
                throw new IllegalArgumentException("Access denied: you can only view teams of your own club");
            }
            return teamRepository.findByClubIdAndActiveTrue(clubId, pageable)
                    .map(this::toTeamResponse);
        }

        if (isCoach) {
            // Coaches see only teams they are assigned to
            List<UUID> assignedTeamIds = teamCoachRepository.findByUserId(currentUser.getId()).stream()
                    .map(tc -> tc.getTeam().getId())
                    .toList();

            if (assignedTeamIds.isEmpty()) {
                return Page.empty(pageable);
            }

            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "year"));

            return teamRepository.findAll(
                    (root, query, cb) -> cb.and(
                            root.get("id").in(assignedTeamIds),
                            cb.equal(root.get("club").get("id"), clubId),
                            cb.isTrue(root.get("active"))
                    ),
                    sortedPageable
            ).map(this::toTeamResponse);
        }

        throw new IllegalArgumentException("Access denied");
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID clubId, UUID teamId) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));
        return toTeamResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(UUID clubId, UUID teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        if (request.getName() != null) {
            if (teamRepository.existsByNameAndClubIdAndIdNot(request.getName(), clubId, teamId)) {
                throw new IllegalArgumentException("Team already exists with name '" + request.getName() + "' in this club");
            }
            team.setName(request.getName());
        }
        if (request.getYear() != null) {
            team.setYear(request.getYear());
        }
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        team = teamRepository.save(team);
        log.info("Updated team '{}' in club '{}'", team.getName(), team.getClub().getName());

        return toTeamResponse(team);
    }

    @Transactional
    public TeamResponse setActive(UUID clubId, UUID teamId, boolean active) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        team.setActive(active);
        team = teamRepository.save(team);

        log.info("Team '{}' active status set to {}", team.getName(), active);

        return toTeamResponse(team);
    }

    @Transactional
    public TeamResponse uploadLogo(UUID clubId, UUID teamId, MultipartFile file) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        // Delete old logo if exists
        if (team.getLogoPath() != null) {
            fileStorage.delete(team.getLogoPath());
        }

        String logoPath = fileStorage.save(file, team.getId(), FileType.TEAM_LOGO);
        team.setLogoPath(logoPath);
        team = teamRepository.save(team);

        log.info("Uploaded logo for team '{}'", team.getName());

        return toTeamResponse(team);
    }

    public Resource getLogo(UUID clubId, UUID teamId) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        if (team.getLogoPath() == null) {
            throw new IllegalArgumentException("Team has no logo: " + teamId);
        }

        return fileStorage.getResource(team.getLogoPath());
    }

    @Transactional
    public TeamResponse assignCoach(UUID clubId, UUID teamId, UUID userId) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (teamCoachRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("User is already assigned as coach to this team");
        }

        TeamCoach teamCoach = new TeamCoach(team, user);
        teamCoachRepository.save(teamCoach);

        log.info("Assigned coach '{}' to team '{}'", user.getUsername(), team.getName());

        return toTeamResponse(team);
    }

    @Transactional
    public TeamResponse removeCoach(UUID clubId, UUID teamId, UUID userId) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        if (!teamCoachRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("User is not assigned as coach to this team");
        }

        teamCoachRepository.deleteByTeamIdAndUserId(teamId, userId);

        log.info("Removed coach '{}' from team '{}'", userId, team.getName());

        return toTeamResponse(team);
    }

    @Transactional(readOnly = true)
    public List<CoachResponse> getCoaches(UUID clubId, UUID teamId) {
        Team team = teamRepository.findByIdAndClubId(teamId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId + " in club: " + clubId));

        return teamCoachRepository.findByTeamId(teamId).stream()
                .map(tc -> new CoachResponse(
                        tc.getUser().getId(),
                        tc.getUser().getUsername(),
                        tc.getUser().getEmail()
                ))
                .toList();
    }

    private TeamResponse toTeamResponse(Team team) {
        List<UUID> coachIds = teamCoachRepository.findByTeamId(team.getId()).stream()
                .map(tc -> tc.getUser().getId())
                .toList();

        String logoUrl = team.getLogoPath() != null
                ? "/api/v1/clubs/" + team.getClub().getId() + "/teams/" + team.getId() + "/logo"
                : null;

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getYear(),
                team.getDescription(),
                logoUrl,
                team.isActive(),
                team.getClub().getId(),
                coachIds,
                team.getCreatedAt(),
                team.getUpdatedAt()
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