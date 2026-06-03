package com.par.jbfh.standard.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.dto.CreateStandardRequest;
import com.par.jbfh.standard.dto.StandardResponse;
import com.par.jbfh.standard.dto.UpdateStandardRequest;
import com.par.jbfh.standard.entity.Standard;
import com.par.jbfh.standard.repository.StandardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardService {

    private final StandardRepository standardRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public StandardResponse create(CreateStandardRequest request) {
        Standard standard = new Standard();
        standard.setName(request.getName());
        standard.setType(request.getType());
        standard.setBirthYear(request.getBirthYear());
        standard.setUnit(request.getUnit());
        standard.setControlValue(request.getControlValue());

        if (request.getClubId() != null) {
            Club club = clubRepository.findById(request.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
            standard.setClub(club);
        }

        standard = standardRepository.save(standard);
        log.info("Created standard '{}' (birthYear={}, type={})", standard.getName(), standard.getBirthYear(), standard.getType());
        return toResponse(standard);
    }

    @Transactional(readOnly = true)
    public Page<StandardResponse> getAll(Integer birthYear, ExerciseType type, Pageable pageable) {
        Page<Standard> page;
        if (birthYear != null && type != null) {
            page = standardRepository.findByBirthYearAndType(birthYear, type, pageable);
        } else if (birthYear != null) {
            page = standardRepository.findByBirthYear(birthYear, pageable);
        } else if (type != null) {
            page = standardRepository.findByType(type, pageable);
        } else {
            page = standardRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StandardResponse getById(UUID id) {
        Standard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standard not found: " + id));
        return toResponse(standard);
    }

    @Transactional
    public StandardResponse update(UUID id, UpdateStandardRequest request) {
        Standard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standard not found: " + id));

        if (request.getName() != null) standard.setName(request.getName());
        if (request.getType() != null) standard.setType(request.getType());
        if (request.getBirthYear() != null) standard.setBirthYear(request.getBirthYear());
        if (request.getUnit() != null) standard.setUnit(request.getUnit());
        if (request.getControlValue() != null) standard.setControlValue(request.getControlValue());

        standard = standardRepository.save(standard);
        log.info("Updated standard '{}'", standard.getName());
        return toResponse(standard);
    }

    @Transactional
    public void delete(UUID id) {
        Standard standard = standardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standard not found: " + id));
        standardRepository.delete(standard);
        log.info("Deleted standard '{}'", standard.getName());
    }

    private StandardResponse toResponse(Standard standard) {
        return new StandardResponse(
                standard.getId(),
                standard.getName(),
                standard.getType(),
                standard.getBirthYear(),
                standard.getUnit(),
                standard.getControlValue(),
                standard.getClub() != null ? standard.getClub().getId() : null,
                standard.getClub() != null ? standard.getClub().getName() : null,
                standard.getCreatedAt()
        );
    }
}