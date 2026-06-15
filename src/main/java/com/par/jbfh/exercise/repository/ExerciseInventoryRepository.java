package com.par.jbfh.exercise.repository;

import com.par.jbfh.exercise.entity.ExerciseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExerciseInventoryRepository extends JpaRepository<ExerciseInventory, UUID> {

    List<ExerciseInventory> findByExerciseId(UUID exerciseId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ExerciseInventory ei WHERE ei.exercise.id = :exerciseId")
    void deleteByExerciseId(@Param("exerciseId") UUID exerciseId);

    List<ExerciseInventory> findByInventoryId(UUID inventoryId);
}
