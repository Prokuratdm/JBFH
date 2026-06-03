package com.par.jbfh.training.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class UpdateTrainingRequest {

    private String name;

    private LocalDate date;

    private LocalTime timeStart;

    private LocalTime timeEnd;

    private UUID locationId;

    private String description;

    private String task1;
    private String task2;
    private String task3;
}