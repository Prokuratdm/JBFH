package com.par.jbfh.training.dto;

import com.par.jbfh.training.enums.Intensity;
import com.par.jbfh.training.enums.LoadLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddExerciseToTrainingRequest {

    @NotNull
    private UUID exerciseId;

    @NotNull
    private Integer workDuration;

    @NotNull
    private Intensity intensity;

    private Integer explanationDuration;

    @NotNull
    private LoadLevel loadLevel;
}