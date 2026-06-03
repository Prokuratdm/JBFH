package com.par.jbfh.exercise.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateExerciseRequest {

    private String name;

    private String description;

    private ExerciseType type;

    private List<UUID> inventoryIds;
}
