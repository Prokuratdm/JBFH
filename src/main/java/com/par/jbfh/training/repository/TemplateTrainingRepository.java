package com.par.jbfh.training.repository;

import com.par.jbfh.training.entity.TemplateTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TemplateTrainingRepository extends JpaRepository<TemplateTraining, UUID> {
}