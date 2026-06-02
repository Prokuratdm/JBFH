package com.par.jbfh.club.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.club.dto.ClubResponse;
import com.par.jbfh.club.dto.CreateClubRequest;
import com.par.jbfh.club.dto.UpdateClubRequest;
import com.par.jbfh.storage.FileStorage;
import com.par.jbfh.storage.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final FileStorage fileStorage;

    @Transactional
    public ClubResponse createClub(CreateClubRequest request) {
        if (clubRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Club already exists with name: " + request.getName());
        }

        Club club = new Club(request.getName());
        club.setAddress(request.getAddress());
        club.setDescription(request.getDescription());
        club = clubRepository.save(club);

        log.info("Created club '{}'", club.getName());

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
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));

        // Delete old logo if exists
        if (club.getLogoPath() != null) {
            fileStorage.delete(club.getLogoPath());
        }

        // Save new logo
        String logoPath = fileStorage.save(file, club.getId(), FileType.CLUB_LOGO);
        club.setLogoPath(logoPath);
        club = clubRepository.save(club);

        log.info("Uploaded logo for club '{}'", club.getName());

        return toClubResponse(club);
    }

    public Resource getLogo(UUID id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + id));

        if (club.getLogoPath() == null) {
            throw new IllegalArgumentException("Club has no logo: " + id);
        }

        return fileStorage.getResource(club.getLogoPath());
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