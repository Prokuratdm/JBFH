package com.par.jbfh.location.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.location.dto.CreateLocationRequest;
import com.par.jbfh.location.dto.LocationResponse;
import com.par.jbfh.location.dto.UpdateLocationRequest;
import com.par.jbfh.location.entity.Location;
import com.par.jbfh.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public LocationResponse create(UUID clubId, CreateLocationRequest request) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found: " + clubId));

        if (locationRepository.existsByNameAndClubId(request.getName(), clubId)) {
            throw new IllegalArgumentException("Location already exists with name '" + request.getName() + "' in this club");
        }

        Location location = new Location();
        location.setName(request.getName());
        location.setClub(club);
        location.setActive(true);
        location = locationRepository.save(location);

        log.info("Created location '{}' in club '{}'", location.getName(), club.getName());
        return toResponse(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAll(UUID clubId, boolean includeInactive) {
        List<Location> locations;
        if (includeInactive) {
            locations = locationRepository.findByClubId(clubId);
        } else {
            locations = locationRepository.findByClubIdAndActiveTrue(clubId);
        }
        return locations.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(UUID clubId, UUID id) {
        Location location = locationRepository.findByIdAndClubId(id, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id + " in club: " + clubId));
        return toResponse(location);
    }

    @Transactional
    public LocationResponse update(UUID clubId, UUID id, UpdateLocationRequest request) {
        Location location = locationRepository.findByIdAndClubId(id, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id + " in club: " + clubId));

        if (request.getName() != null) {
            if (locationRepository.existsByNameAndClubIdAndIdNot(request.getName(), clubId, id)) {
                throw new IllegalArgumentException("Location already exists with name '" + request.getName() + "' in this club");
            }
            location.setName(request.getName());
        }

        location = locationRepository.save(location);
        log.info("Updated location '{}'", location.getName());
        return toResponse(location);
    }

    @Transactional
    public LocationResponse setActive(UUID clubId, UUID id, boolean active) {
        Location location = locationRepository.findByIdAndClubId(id, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id + " in club: " + clubId));

        location.setActive(active);
        location = locationRepository.save(location);
        log.info("Location '{}' active status set to {}", location.getName(), active);
        return toResponse(location);
    }

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.isActive(),
                location.getClub().getId(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}