package com.par.jbfh.training.controller;

import com.par.jbfh.training.dto.CreateTemplateTrainingRequest;
import com.par.jbfh.training.dto.TemplateTrainingResponse;
import com.par.jbfh.training.dto.TrainingResponse;
import com.par.jbfh.training.service.TemplateTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/template-trainings")
@RequiredArgsConstructor
@Tag(name = "Template Trainings", description = "Template trainings management API")
public class TemplateTrainingController {

    private final TemplateTrainingService templateTrainingService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Create template training")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateTrainingResponse create(@Valid @RequestBody CreateTemplateTrainingRequest request) {
        return templateTrainingService.create(request);
    }

    @GetMapping
    @Operation(summary = "List template trainings")
    public Page<TemplateTrainingResponse> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return templateTrainingService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template training")
    public TemplateTrainingResponse getById(@PathVariable UUID id) {
        return templateTrainingService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Update template training")
    public TemplateTrainingResponse update(@PathVariable UUID id,
                                           @Valid @RequestBody CreateTemplateTrainingRequest request) {
        return templateTrainingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST"})
    @Operation(summary = "Delete template training")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        templateTrainingService.delete(id);
    }

    @PostMapping("/{id}/apply/{teamId}")
    @Secured({"ROLE_CLUB", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Apply template", description = "Create a training from a template for a team")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingResponse apply(@PathVariable UUID id, @PathVariable UUID teamId) {
        return templateTrainingService.apply(id, teamId);
    }
}