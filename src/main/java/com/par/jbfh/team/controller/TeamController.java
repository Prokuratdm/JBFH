package com.par.jbfh.team.controller;

import com.par.jbfh.team.dto.*;
import com.par.jbfh.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Teams management API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Secured("ROLE_CLUB")
    @Operation(summary = "Create team", description = "Create a new team in a club. Club representative only.")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@PathVariable UUID clubId, @Valid @RequestBody CreateTeamRequest request) {
        return teamService.createTeam(clubId, request);
    }

    @GetMapping
    @Secured({"ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all teams", description = "Returns a paginated list of teams in a club. Default sort by year DESC (youngest first).")
    public Page<TeamResponse> getTeams(
            @PathVariable UUID clubId,
            @PageableDefault(size = 18, sort = "year", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return teamService.getTeamsByClub(clubId, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get team by ID", description = "Returns team details.")
    public TeamResponse getTeamById(@PathVariable UUID clubId, @PathVariable UUID id) {
        return teamService.getTeamById(clubId, id);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Update team", description = "Update team name, year, and/or description. Club representative only.")
    public TeamResponse updateTeam(@PathVariable UUID clubId, @PathVariable UUID id, @Valid @RequestBody UpdateTeamRequest request) {
        return teamService.updateTeam(clubId, id, request);
    }

    @PatchMapping("/{id}/active")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Activate or deactivate team", description = "Set team active status. Club representative only.")
    public TeamResponse setActive(@PathVariable UUID clubId, @PathVariable UUID id, @RequestParam boolean active) {
        return teamService.setActive(clubId, id, active);
    }

    @PostMapping("/{id}/logo")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Upload team logo", description = "Upload or update team logo. Max size 200KB. Image files only.")
    public TeamResponse uploadLogo(@PathVariable UUID clubId, @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return teamService.uploadLogo(clubId, id, file);
    }

    @GetMapping("/{id}/logo")
    @Secured({"ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get team logo", description = "Returns the team logo image file.")
    public ResponseEntity<Resource> getLogo(@PathVariable UUID clubId, @PathVariable UUID id) {
        Resource resource = teamService.getLogo(clubId, id);
        String contentType = "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/coaches")
    @Secured({"ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Get team coaches", description = "Returns the list of coaches assigned to the team.")
    public List<CoachResponse> getCoaches(@PathVariable UUID clubId, @PathVariable UUID id) {
        return teamService.getCoaches(clubId, id);
    }

    @PostMapping("/{id}/coaches")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Assign coach to team", description = "Assign a user as coach to the team. Club representative only.")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse assignCoach(@PathVariable UUID clubId, @PathVariable UUID id, @Valid @RequestBody AssignCoachRequest request) {
        return teamService.assignCoach(clubId, id, request.getUserId());
    }

    @DeleteMapping("/{id}/coaches/{userId}")
    @Secured("ROLE_CLUB")
    @Operation(summary = "Remove coach from team", description = "Remove a coach from the team. Club representative only.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCoach(@PathVariable UUID clubId, @PathVariable UUID id, @PathVariable UUID userId) {
        teamService.removeCoach(clubId, id, userId);
    }
}