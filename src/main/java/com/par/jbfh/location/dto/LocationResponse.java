package com.par.jbfh.location.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String name,
        boolean active,
        UUID clubId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}