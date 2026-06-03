package com.par.jbfh.child.service;

import com.par.jbfh.child.dto.ChildResponse;
import com.par.jbfh.child.dto.CreateChildRequest;
import com.par.jbfh.child.dto.UpdateChildRequest;
import com.par.jbfh.child.entity.Child;
import com.par.jbfh.child.repository.ChildRepository;
import com.par.jbfh.team.entity.Team;
import com.par.jbfh.team.repository.TeamRepository;
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
public class ChildService {

    private final ChildRepository childRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public ChildResponse create(UUID teamId, CreateChildRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        Child child = new Child();
        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setMiddleName(request.getMiddleName());
        child.setBirthYear(request.getBirthYear());
        child.setGender(request.getGender());
        child.setTeam(team);

        child = childRepository.save(child);
        log.info("Created child '{} {}' in team '{}'", child.getFirstName(), child.getLastName(), team.getName());
        return toResponse(child);
    }

    @Transactional(readOnly = true)
    public Page<ChildResponse> getByTeam(UUID teamId, Pageable pageable) {
        return childRepository.findByTeamId(teamId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ChildResponse getById(UUID teamId, UUID id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + id));
        if (!child.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Child does not belong to team: " + teamId);
        }
        return toResponse(child);
    }

    @Transactional
    public ChildResponse update(UUID teamId, UUID id, UpdateChildRequest request) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + id));
        if (!child.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Child does not belong to team: " + teamId);
        }

        if (request.getFirstName() != null) child.setFirstName(request.getFirstName());
        if (request.getLastName() != null) child.setLastName(request.getLastName());
        if (request.getMiddleName() != null) child.setMiddleName(request.getMiddleName());
        if (request.getBirthYear() != null) child.setBirthYear(request.getBirthYear());
        if (request.getGender() != null) child.setGender(request.getGender());

        child = childRepository.save(child);
        log.info("Updated child '{} {}'", child.getFirstName(), child.getLastName());
        return toResponse(child);
    }

    @Transactional
    public void delete(UUID teamId, UUID id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + id));
        if (!child.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Child does not belong to team: " + teamId);
        }
        childRepository.delete(child);
        log.info("Deleted child '{} {}'", child.getFirstName(), child.getLastName());
    }

    private ChildResponse toResponse(Child child) {
        return new ChildResponse(
                child.getId(),
                child.getFirstName(),
                child.getLastName(),
                child.getMiddleName(),
                child.getBirthYear(),
                child.getGender(),
                child.getTeam().getId(),
                child.getTeam().getName(),
                child.getCreatedAt()
        );
    }
}