package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.TemplateTrainingExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TemplateTrainingExerciseRepository extends JpaRepository<TemplateTrainingExercise, UUID> {

    List<TemplateTrainingExercise> findByTemplateTrainingId(UUID templateTrainingId);

    void deleteByTemplateTrainingId(UUID templateTrainingId);
}