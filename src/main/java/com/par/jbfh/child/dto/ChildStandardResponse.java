package com.par.jbfh.child.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ChildStandardResponse(
        UUID id,
        UUID childId,
        UUID standardId,
        String standardName,
        String standardUnit,
        BigDecimal controlValue,
        BigDecimal resultValue,
        LocalDate date
) {
}