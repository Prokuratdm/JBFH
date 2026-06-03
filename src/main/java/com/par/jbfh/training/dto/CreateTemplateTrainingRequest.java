package com.par.jbfh.training.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTemplateTrainingRequest {

    @NotBlank
    private String name;

    private String description;

    private String task1;
    private String task2;
    private String task3;

    private UUID clubId;
}