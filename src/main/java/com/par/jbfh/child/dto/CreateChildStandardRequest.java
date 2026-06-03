package com.par.jbfh.child.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateChildStandardRequest {

    @NotNull
    private UUID standardId;

    @NotNull
    private BigDecimal resultValue;

    @NotNull
    private LocalDate date;
}