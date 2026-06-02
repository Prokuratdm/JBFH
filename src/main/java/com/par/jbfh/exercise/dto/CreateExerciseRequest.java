package com.par.jbfh.exercise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateExerciseRequest {

    @NotBlank
    private String name;

    private String description;

    private List<UUID> inventoryIds;

    private UUID clubId;
}