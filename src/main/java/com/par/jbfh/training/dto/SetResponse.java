package com.par.jbfh.training.dto;

import com.par.jbfh.exercise.enums.TrainingPart;

import java.time.LocalDateTime;
import java.util.UUID;

public record SetResponse(
        UUID id,
        String name,
        TrainingPart trainingPart,
        UUID clubId,
        String clubName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}