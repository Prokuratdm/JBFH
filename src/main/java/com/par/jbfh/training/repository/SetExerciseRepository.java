package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.SetExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SetExerciseRepository extends JpaRepository<SetExercise, UUID> {

    List<SetExercise> findBySetId(UUID setId);

    void deleteBySetId(UUID setId);
}