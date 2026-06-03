package com.par.jbfh.child.service;

import com.par.jbfh.child.dto.CreatePhysicalStatsRequest;
import com.par.jbfh.child.dto.PhysicalStatsResponse;
import com.par.jbfh.child.entity.Child;
import com.par.jbfh.child.entity.ChildPhysicalStats;
import com.par.jbfh.child.repository.ChildPhysicalStatsRepository;
import com.par.jbfh.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChildPhysicalStatsService {

    private final ChildPhysicalStatsRepository statsRepository;
    private final ChildRepository childRepository;

    @Transactional
    public PhysicalStatsResponse create(UUID childId, CreatePhysicalStatsRequest request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found: " + childId));

        ChildPhysicalStats stats = new ChildPhysicalStats();
        stats.setChild(child);
        stats.setHeight(request.getHeight());
        stats.setWeight(request.getWeight());
        stats.setDate(request.getDate());

        stats = statsRepository.save(stats);
        log.info("Recorded physical stats for child '{} {}'", child.getFirstName(), child.getLastName());
        return toResponse(stats);
    }

    @Transactional(readOnly = true)
    public List<PhysicalStatsResponse> getByChild(UUID childId) {
        return statsRepository.findByChildIdOrderByDateDesc(childId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID childId, UUID statsId) {
        ChildPhysicalStats stats = statsRepository.findById(statsId)
                .orElseThrow(() -> new IllegalArgumentException("Physical stats not found: " + statsId));
        if (!stats.getChild().getId().equals(childId)) {
            throw new IllegalArgumentException("Stats do not belong to child: " + childId);
        }
        statsRepository.delete(stats);
        log.info("Deleted physical stats record for child '{} {}'",
                stats.getChild().getFirstName(), stats.getChild().getLastName());
    }

    private PhysicalStatsResponse toResponse(ChildPhysicalStats stats) {
        return new PhysicalStatsResponse(
                stats.getId(),
                stats.getChild().getId(),
                stats.getHeight(),
                stats.getWeight(),
                stats.getDate()
        );
    }
}