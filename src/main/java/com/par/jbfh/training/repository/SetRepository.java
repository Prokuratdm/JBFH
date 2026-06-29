package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetRepository extends JpaRepository<Set, UUID> {
    boolean existsByName(String name);
}