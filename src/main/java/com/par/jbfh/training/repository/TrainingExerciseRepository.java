package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.TrainingExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainingExerciseRepository extends JpaRepository<TrainingExercise, UUID> {

    List<TrainingExercise> findByTrainingId(UUID trainingId);
}