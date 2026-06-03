package com.par.jbfh.exercise.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateExerciseRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private ExerciseType type;

    private List<UUID> inventoryIds;

    private UUID clubId;
}
