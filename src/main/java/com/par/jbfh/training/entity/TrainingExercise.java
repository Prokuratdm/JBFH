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
@Table(name = "training_exercises")
@Getter
@Setter
@NoArgsConstructor
public class TrainingExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

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

    @PrePersist
    @PreUpdate
    protected void calculate() {
        // rest_duration = work_duration / 2 (автоматически)
        this.restDuration = this.workDuration / 2;
        // work_mode определяется автоматически
        if (this.workDuration >= 120) {
            this.workMode = WorkMode.UNIFORM;
        } else if (this.workDuration >= 60) {
            this.workMode = WorkMode.ALTERNATING;
        } else if (this.workDuration >= 30) {
            this.workMode = WorkMode.INTERVAL;
        } else {
            this.workMode = WorkMode.REPEATED;
        }
        // total_time = work_duration + rest_duration + explanation_duration
        int expl = this.explanationDuration != null ? this.explanationDuration : 0;
        int rest = this.restDuration != null ? this.restDuration : 0;
        this.totalTime = this.workDuration + rest + expl;
    }
}