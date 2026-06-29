package com.par.jbfh.training.entity;

import com.par.jbfh.exercise.entity.Exercise;
import com.par.jbfh.training.enums.Intensity;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.WorkMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "template_training_exercises")
@Getter
@Setter
@NoArgsConstructor
public class TemplateTrainingExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_training_id", nullable = false)
    private TemplateTraining templateTraining;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "work_duration", nullable = false)
    private int workDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Intensity intensity;

    @Column(name = "rest_duration")
    private Integer restDuration;

    @Column(name = "explanation_duration")
    private Integer explanationDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 50)
    private WorkMode workMode;

    @Column(name = "total_time")
    private Integer totalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_level", nullable = false, length = 50)
    private LoadLevel loadLevel;

    @Column(nullable = false)
    private int repetitions = 1;
}