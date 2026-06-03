package com.par.jbfh.training.dto;

import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TrainingProgramResponse(
        UUID id,
        int birthYear,
        LoadLevel loadLevel,
        TrainingCycle cycle,
        BigDecimal percentage,
        UUID clubId,
        String clubName,
        LocalDateTime createdAt
) {
}