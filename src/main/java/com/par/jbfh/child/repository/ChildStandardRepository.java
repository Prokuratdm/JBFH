package com.par.jbfh.child.repository;

import com.par.jbfh.child.entity.ChildStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChildStandardRepository extends JpaRepository<ChildStandard, UUID> {

    List<ChildStandard> findByChildId(UUID childId);
}