package com.par.jbfh.child.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PhysicalStatsResponse(
        UUID id,
        UUID childId,
        BigDecimal height,
        BigDecimal weight,
        LocalDate date
) {
}