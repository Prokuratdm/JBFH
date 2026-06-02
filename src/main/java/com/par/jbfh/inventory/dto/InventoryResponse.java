package com.par.jbfh.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        String name,
        boolean active,
        UUID clubId,
        String clubName,
        LocalDateTime createdAt
) {
}