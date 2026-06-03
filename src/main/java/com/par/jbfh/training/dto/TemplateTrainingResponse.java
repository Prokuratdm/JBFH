package com.par.jbfh.training.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemplateTrainingResponse(
        UUID id,
        String name,
        String description,
        String pictureUrl,
        String task1,
        String task2,
        String task3,
        UUID clubId,
        String clubName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}