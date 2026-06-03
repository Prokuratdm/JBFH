package com.par.jbfh.training.dto;

import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTrainingProgramRequest {

    @NotNull
    private Integer birthYear;

    @NotNull
    private LoadLevel loadLevel;

    @NotNull
    private TrainingCycle cycle;

    @NotNull
    private BigDecimal percentage;

    private UUID clubId;
}