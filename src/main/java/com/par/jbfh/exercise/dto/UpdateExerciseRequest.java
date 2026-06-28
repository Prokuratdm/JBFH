package com.par.jbfh.exercise.dto;

import com.par.jbfh.exercise.enums.ExerciseType;
import com.par.jbfh.exercise.enums.Focus;
import com.par.jbfh.exercise.enums.PreparationType;
import com.par.jbfh.exercise.enums.TrainingPart;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class UpdateExerciseRequest {

    private String name;

    private String description;

    private ExerciseType type;

    private String url;

    private String content;

    private List<UUID> inventoryIds;

    private TrainingPart trainingPart;

    private Set<Focus> focuses;

    private PreparationType preparationType;
}
