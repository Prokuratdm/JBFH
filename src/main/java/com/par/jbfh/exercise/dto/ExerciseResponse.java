package com.par.jbfh.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        String pictureUrl,
        boolean active,
        UUID clubId,
        String clubName,
        List<UUID> inventoryIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}