package com.par.jbfh.exercise.repository;

import com.par.jbfh.exercise.entity.ExerciseInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseInventoryRepository extends JpaRepository<ExerciseInventory, UUID> {

    List<ExerciseInventory> findByExerciseId(UUID exerciseId);

    void deleteByExerciseId(UUID exerciseId);

    List<ExerciseInventory> findByInventoryId(UUID inventoryId);
}