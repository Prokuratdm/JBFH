package com.par.jbfh.club.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.RoleRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.club.dto.ClubResponse;
import com.par.jbfh.club.dto.CreateClubRequest;
import com.par.jbfh.club.dto.UpdateClubRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.logo-path:uploads/logos}")
    private String logoUploadPath;

    @Transactional
    public ClubResponse createClub(CreateClubRequest request) {
        if (clubRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Club already exists with name: " + request.getName());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Create club
        Club club = new Club(request.getName());
        club.setAddress(request.getAddress());
        club.setDescription(request.getDescription());
        club = clubRepository.save(club);

        // Create club user
        Role clubRole = roleRepository.findByName("ROLE_CLUB")
                .orElseThrow(() -> new IllegalArgumentException("ROLE_CLUB not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setClub(club);
        user.setRoles(Set.of(clubRole));
        userRepository.save(user);

        log.info("Created club '{}' with user '{}'", club.getName(), user.getUsername());

        return toClubResponse(club);
    }

    @Transactional(readOnly = true)
    public Page<ClubResponse> getAllClubs(Pageable pageable) {
        return clubRepository.findAll(pageable)
                .map(this::toClubResponse);
    }

    @Transactional(readOnly = true)
    public ClubResponse getClubById(UUID id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));
        return toClubResponse(club);
    }

    @Transactional
    public ClubResponse updateClub(UUID id, UpdateClubRequest request) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));

        if (request.getAddress() != null) {
            club.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            club.setDescription(request.getDescription());
        }

        club = clubRepository.save(club);
        log.info("Updated club '{}'", club.getName());

        return toClubResponse(club);
    }

    @Transactional
    public ClubResponse uploadLogo(UUID id, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file size (max 200KB = 200 * 1024 bytes)
        if (file.getSize() > 200 * 1024) {
            throw new IllegalArgumentException("File size exceeds 200KB limit");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));

        try {
            // Ensure upload directory exists
            Path uploadDir = Paths.get(logoUploadPath);
            Files.createDirectories(uploadDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = club.getId() + "_" + System.currentTimeMillis() + extension;
            Path targetPath = uploadDir.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Delete old logo if exists
            if (club.getLogoPath() != null) {
                Path oldPath = Paths.get(club.getLogoPath());
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    log.warn("Failed to delete old logo: {}", oldPath, e);
                }
            }

            club.setLogoPath(targetPath.toString());
            club = clubRepository.save(club);

            log.info("Uploaded logo for club '{}': {}", club.getName(), filename);

            return toClubResponse(club);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload logo", e);
        }
    }

    public Resource getLogo(UUID id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));

        if (club.getLogoPath() == null) {
            throw new IllegalArgumentException("Club has no logo: " + id);
        }

        Path path = Paths.get(club.getLogoPath());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Logo file not found for club: " + id);
        }

        return new FileSystemResource(path);
    }

    private ClubResponse toClubResponse(Club club) {
        String logoUrl = club.getLogoPath() != null
                ? "/api/v1/clubs/" + club.getId() + "/logo"
                : null;

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getAddress(),
                club.getDescription(),
                logoUrl,
                club.getCreatedAt(),
                club.getUpdatedAt()
        );
    }
}