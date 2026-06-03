package com.par.jbfh.child.repository;

import com.par.jbfh.child.entity.Child;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChildRepository extends JpaRepository<Child, UUID> {

    Page<Child> findByTeamId(UUID teamId, Pageable pageable);
}