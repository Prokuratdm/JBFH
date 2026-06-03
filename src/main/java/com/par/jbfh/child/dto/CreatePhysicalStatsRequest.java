package com.par.jbfh.child.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePhysicalStatsRequest {

    @NotNull
    private BigDecimal height;

    @NotNull
    private BigDecimal weight;

    @NotNull
    private LocalDate date;
}