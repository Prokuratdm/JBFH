package com.par.jbfh.child.service;

import com.par.jbfh.child.dto.ChildStandardResponse;
import com.par.jbfh.child.dto.CreateChildStandardRequest;
import com.par.jbfh.child.entity.Child;
import com.par.jbfh.child.entity.ChildStandard;
import com.par.jbfh.child.repository.ChildRepository;
import com.par.jbfh.child.repository.ChildStandardRepository;
import com.par.jbfh.standard.entity.Standard;
import com.par.jbfh.standard.repository.StandardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChildStandardService {

    private final ChildStandardRepository childStandardRepository;
    private final ChildRepository childRepository;
    private final StandardRepository standardRepository;

    @Transactional
    public ChildStandardResponse create(UUID childId, CreateChildStandardRequest request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + childId));

        Standard standard = standardRepository.findById(request.getStandardId())
                .orElseThrow(() -> new IllegalArgumentException("Standard not found: " + request.getStandardId()));

        ChildStandard childStandard = new ChildStandard();
        childStandard.setChild(child);
        childStandard.setStandard(standard);
        childStandard.setResultValue(request.getResultValue());
        childStandard.setDate(request.getDate());

        childStandard = childStandardRepository.save(childStandard);
        log.info("Recorded standard result for child '{} {}': standard='{}'",
                child.getFirstName(), child.getLastName(), standard.getName());
        return toResponse(childStandard);
    }

    @Transactional(readOnly = true)
    public List<ChildStandardResponse> getByChild(UUID childId) {
        return childStandardRepository.findByChildId(childId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChildStandardResponse update(UUID childId, UUID id, CreateChildStandardRequest request) {
        ChildStandard cs = childStandardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChildStandard not found: " + id));
        if (!cs.getChild().getId().equals(childId)) {
            throw new IllegalArgumentException("Result does not belong to child: " + childId);
        }

        cs.setResultValue(request.getResultValue());
        cs.setDate(request.getDate());

        cs = childStandardRepository.save(cs);
        log.info("Updated standard result for child '{} {}'", cs.getChild().getFirstName(), cs.getChild().getLastName());
        return toResponse(cs);
    }

    @Transactional
    public void delete(UUID childId, UUID id) {
        ChildStandard cs = childStandardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChildStandard not found: " + id));
        if (!cs.getChild().getId().equals(childId)) {
            throw new IllegalArgumentException("Result does not belong to child: " + childId);
        }
        childStandardRepository.delete(cs);
        log.info("Deleted standard result for child '{} {}'",
                cs.getChild().getFirstName(), cs.getChild().getLastName());
    }

    private ChildStandardResponse toResponse(ChildStandard cs) {
        return new ChildStandardResponse(
                cs.getId(),
                cs.getChild().getId(),
                cs.getStandard().getId(),
                cs.getStandard().getName(),
                cs.getStandard().getUnit().name(),
                cs.getStandard().getControlValue(),
                cs.getResultValue(),
                cs.getDate()
        );
    }
}