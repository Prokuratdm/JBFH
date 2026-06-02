package com.par.jbfh.exercise.repository;

import com.par.jbfh.exercise.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    @Query("SELECT e FROM Exercise e WHERE e.active = true AND (e.club IS NULL OR e.club.id = :clubId)")
    Page<Exercise> findVisibleForClub(@Param("clubId") UUID clubId, Pageable pageable);

    @Query("SELECT e FROM Exercise e WHERE e.club IS NULL OR e.club.id = :clubId")
    Page<Exercise> findAllVisibleForClub(@Param("clubId") UUID clubId, Pageable pageable);

    Page<Exercise> findByActiveTrue(Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}