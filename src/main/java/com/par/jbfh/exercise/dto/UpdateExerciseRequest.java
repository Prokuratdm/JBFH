package com.par.jbfh.exercise.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateExerciseRequest {

    private String name;

    private String description;

    private List<UUID> inventoryIds;
}