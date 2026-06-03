package com.par.jbfh.standard.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.enums.StandardUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StandardResponse(
        UUID id,
        String name,
        ExerciseType type,
        int birthYear,
        StandardUnit unit,
        BigDecimal controlValue,
        UUID clubId,
        String clubName,
        LocalDateTime createdAt
) {
}