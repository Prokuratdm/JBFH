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

    @Column(nullable = false)
    private int repetitions = 1;

    @PrePersist
    @PreUpdate
    protected void calculate() {
        var calc = CalculationResult.calculate(this.workDuration, this.intensity);
        this.restDuration = calc.restDuration;
        this.workMode = calc.workMode;

        int expl = this.explanationDuration != null ? this.explanationDuration : 0;
        this.totalTime = this.repetitions * (this.workDuration + this.restDuration) + expl;
    }

    /**
     * Вспомогательный record с результатами расчёта restDuration и workMode.
     * Вынесен для переиспользования в контроллере (калькуляционные ручки).
     */
    public record CalculationResult(int restDuration, WorkMode workMode) {

        // Коэффициенты rest: [Intensity] -> [<=25, <=60, >60]
        private static final int[][] REST_COEFF = {
            // MAXIMUM
            {5, 4, 3},
            // SUBMAXIMUM
            {4, 3, 2},
            // HIGH
            {3, 2, 1},
            // MEDIUM
            {2, 1, 1},
            // LOW
            {1, 1, 0}
        };

        // Режим работы: [Intensity] -> [<=12, 12-25, 25-60, >60]
        private static final WorkMode[][] OPERATING_MODE = {
            // MAXIMUM
            {WorkMode.ANAEROBIC_ALACTIC, WorkMode.ANAEROBIC_MIXED, WorkMode.ANAEROBIC_MIXED, WorkMode.ANAEROBIC_LACTIC},
            // SUBMAXIMUM
            {WorkMode.ANAEROBIC_ALACTIC, WorkMode.ANAEROBIC_MIXED, WorkMode.ANAEROBIC_LACTIC, WorkMode.ANAEROBIC_LACTIC},
            // HIGH
            {WorkMode.ANAEROBIC_MIXED, WorkMode.ANAEROBIC_MIXED, WorkMode.ANAEROBIC_LACTIC, WorkMode.ANAEROBIC_LACTIC},
            // MEDIUM
            {WorkMode.AEROBIC, WorkMode.AEROBIC, WorkMode.AEROBIC, WorkMode.AEROBIC},
            // LOW
            {WorkMode.AEROBIC_RECOVERY, WorkMode.AEROBIC_RECOVERY, WorkMode.AEROBIC_RECOVERY, WorkMode.AEROBIC_RECOVERY}
        };

        public static CalculationResult calculate(int workDuration, Intensity intensity) {
            int i = intensity.ordinal();

            int restCoeffIdx;
            if (workDuration <= 25) {
                restCoeffIdx = 0;
            } else if (workDuration <= 60) {
                restCoeffIdx = 1;
            } else {
                restCoeffIdx = 2;
            }
            int restDuration = workDuration * REST_COEFF[i][restCoeffIdx];

            int modeIdx;
            if (workDuration <= 12) {
                modeIdx = 0;
            } else if (workDuration <= 25) {
                modeIdx = 1;
            } else if (workDuration <= 60) {
                modeIdx = 2;
            } else {
                modeIdx = 3;
            }
            WorkMode workMode = OPERATING_MODE[i][modeIdx];

            return new CalculationResult(restDuration, workMode);
        }
    }
}