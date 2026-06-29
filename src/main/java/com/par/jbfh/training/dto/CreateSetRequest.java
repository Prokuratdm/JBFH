package com.par.jbfh.training.dto;

import com.par.jbfh.exercise.enums.TrainingPart;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSetRequest {

    @NotBlank
    private String name;

    private TrainingPart trainingPart;

    private UUID clubId;
}