package com.par.jbfh.standard.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.enums.StandardUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateStandardRequest {

    @NotBlank
    private String name;

    @NotNull
    private ExerciseType type;

    @NotNull
    private Integer birthYear;

    @NotNull
    private StandardUnit unit;

    @NotNull
    private BigDecimal controlValue;

    private UUID clubId;
}