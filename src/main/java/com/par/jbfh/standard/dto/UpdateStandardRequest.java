package com.par.jbfh.standard.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.standard.enums.StandardUnit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateStandardRequest {

    private String name;

    private ExerciseType type;

    private Integer birthYear;

    private StandardUnit unit;

    private BigDecimal controlValue;
}