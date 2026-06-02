package com.par.jbfh.team.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        Integer year,
        String description,
        String logoUrl,
        boolean active,
        UUID clubId,
        List<UUID> coachIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}