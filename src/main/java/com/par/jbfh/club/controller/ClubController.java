package com.par.jbfh.club.controller;

import com.par.jbfh.club.dto.ClubResponse;
import com.par.jbfh.club.dto.CreateClubRequest;
import com.par.jbfh.club.dto.UpdateClubRequest;
import com.par.jbfh.club.service.ClubService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubs", description = "Clubs management API")
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Create club", description = "Create a new club. Admin only.")
    @ResponseStatus(HttpStatus.CREATED)
    public ClubResponse createClub(@Valid @RequestBody CreateClubRequest request) {
        return clubService.createClub(request);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Get all clubs", description = "Returns a paginated list of all clubs. Admin and Methodists only.")
    public Page<ClubResponse> getAllClubs(@PageableDefault(size = 10) Pageable pageable) {
        return clubService.getAllClubs(pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Get club by ID", description = "Returns club details. Admin and Methodists only.")
    public ClubResponse getClubById(@PathVariable UUID id) {
        return clubService.getClubById(id);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Update club", description = "Update club address and/or description. Admin only. Name cannot be changed.")
    public ClubResponse updateClub(@PathVariable UUID id, @Valid @RequestBody UpdateClubRequest request) {
        return clubService.updateClub(id, request);
    }

    @PostMapping("/{id}/logo")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Upload club logo", description = "Upload or update club logo. Max size 200KB. Image files only.")
    public ClubResponse uploadLogo(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return clubService.uploadLogo(id, file);
    }

    @GetMapping("/{id}/logo")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Get club logo", description = "Returns the club logo image file.")
    public ResponseEntity<Resource> getLogo(@PathVariable UUID id) {
        Resource resource = clubService.getLogo(id);
        String contentType = "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}