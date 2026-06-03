package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface TrainingRepository extends JpaRepository<Training, UUID> {

    Page<Training> findByTeamId(UUID teamId, Pageable pageable);

    Page<Training> findByTeamIdAndDateBetween(UUID teamId, LocalDate from, LocalDate to, Pageable pageable);
}