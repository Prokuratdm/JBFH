package com.par.jbfh.training.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record TrainingResponse(
        UUID id,
        String name,
        LocalDate date,
        LocalTime timeStart,
        LocalTime timeEnd,
        UUID locationId,
        String locationName,
        UUID teamId,
        String teamName,
        String pictureUrl,
        String description,
        String task1,
        String task2,
        String task3,
        UUID createdById,
        String createdByUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}