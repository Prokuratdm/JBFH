package com.par.jbfh.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateTrainingRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime timeStart;

    @NotNull
    private LocalTime timeEnd;

    @NotNull
    private UUID locationId;

    private String description;

    private String task1;
    private String task2;
    private String task3;
}
