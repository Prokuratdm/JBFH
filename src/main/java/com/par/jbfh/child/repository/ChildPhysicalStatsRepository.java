package com.par.jbfh.child.repository;

import com.par.jbfh.child.entity.ChildPhysicalStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChildPhysicalStatsRepository extends JpaRepository<ChildPhysicalStats, UUID> {

    List<ChildPhysicalStats> findByChildIdOrderByDateDesc(UUID childId);
}