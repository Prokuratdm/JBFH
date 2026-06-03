package com.par.jbfh.training.dto;

import com.par.jbfh.training.enums.Intensity;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.WorkMode;

import java.util.UUID;

public record TrainingExerciseResponse(
        UUID id,
        UUID trainingId,
        UUID exerciseId,
        String exerciseName,
        int workDuration,
        Intensity intensity,
        Integer restDuration,
        Integer explanationDuration,
        WorkMode workMode,
        Integer totalTime,
        LoadLevel loadLevel
) {
}