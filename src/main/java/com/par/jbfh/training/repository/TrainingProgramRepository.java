package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.TrainingProgram;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, UUID> {

    List<TrainingProgram> findByBirthYearAndLoadLevelAndCycle(int birthYear, LoadLevel loadLevel, TrainingCycle cycle);

    List<TrainingProgram> findByBirthYear(int birthYear);

    List<TrainingProgram> findByLoadLevel(LoadLevel loadLevel);

    List<TrainingProgram> findByCycle(TrainingCycle cycle);
}