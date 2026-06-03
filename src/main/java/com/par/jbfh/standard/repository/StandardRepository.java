package com.par.jbfh.standard.repository;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.entity.Standard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StandardRepository extends JpaRepository<Standard, UUID> {

    Page<Standard> findByBirthYearAndType(int birthYear, ExerciseType type, Pageable pageable);

    Page<Standard> findByBirthYear(int birthYear, Pageable pageable);

    Page<Standard> findByType(ExerciseType type, Pageable pageable);
}