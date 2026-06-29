package com.par.jbfh.training.controller;

import com.par.jbfh.training.enums.Intensity;
import com.par.jbfh.training.service.ExerciseCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/calculator")
@RequiredArgsConstructor
@Tag(name = "Calculator", description = "Training exercise calculation API")
public class CalculatorController {

    private final ExerciseCalculator exerciseCalculator;

    @GetMapping("/rest-and-mode")
    @Operation(summary = "Calculate rest duration and work mode",
            description = "Calculate restDuration and workMode from workDuration and intensity. Does not save any data.")
    public Map<String, Object> calculateRestAndMode(
            @RequestParam int workDuration,
            @RequestParam Intensity intensity) {
        var calc = exerciseCalculator.calculateRestAndMode(workDuration, intensity);
        return Map.of(
                "restDuration", calc.restDuration(),
                "workMode", calc.workMode().name()
        );
    }

    @GetMapping("/total-time")
    @Operation(summary = "Calculate total time",
            description = "Calculate totalTime, restDuration and workMode from all inputs. Does not save any data.")
    public Map<String, Object> calculateTotalTime(
            @RequestParam int workDuration,
            @RequestParam Intensity intensity,
            @RequestParam(defaultValue = "1") int repetitions,
            @RequestParam(required = false) Integer explanationDuration) {
        var calc = exerciseCalculator.calculateRestAndMode(workDuration, intensity);
        int totalTime = exerciseCalculator.calculateTotalTime(workDuration, calc.restDuration(), repetitions, explanationDuration);
        return Map.of(
                "totalTime", totalTime,
                "restDuration", calc.restDuration(),
                "workMode", calc.workMode().name()
        );
    }
}