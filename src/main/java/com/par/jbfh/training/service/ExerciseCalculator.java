package com.par.jbfh.training.service;

import com.par.jbfh.training.enums.Intensity;
import com.par.jbfh.training.enums.WorkMode;
import org.springframework.stereotype.Service;

/**
 * Утилитный сервис расчёта параметров тренировочного упражнения:
 * время отдыха, режим работы, общее время.
 * <p>
 * Вынесен из entity, чтобы переиспользоваться между TrainingExercise,
 * SetExercise, TemplateTrainingExercise и калькуляционными ручками.
 */
@Service
public class ExerciseCalculator {

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

    public record CalcResult(int restDuration, WorkMode workMode) {}

    public CalcResult calculateRestAndMode(int workDuration, Intensity intensity) {
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

        return new CalcResult(restDuration, workMode);
    }

    public int calculateTotalTime(int workDuration, int restDuration,
                                  int repetitions, Integer explanationDuration) {
        int expl = explanationDuration != null ? explanationDuration : 0;
        int reps = repetitions > 0 ? repetitions : 1;
        return reps * (workDuration + restDuration) + expl;
    }
}