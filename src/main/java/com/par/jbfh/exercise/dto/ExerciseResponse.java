package com.par.jbfh.exercise.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.exercise.enums.Focus;
import com.par.jbfh.exercise.enums.PreparationType;
import com.par.jbfh.exercise.enums.TrainingPart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        ExerciseType type,
        String pictureUrl,
        String url,
        String content,
        boolean active,
        UUID clubId,
        String clubName,
        List<UUID> inventoryIds,
        TrainingPart trainingPart,
        Set<Focus> focuses,
        PreparationType preparationType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
