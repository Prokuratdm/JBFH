package com.par.jbfh.training.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.training.dto.CreateTrainingProgramRequest;
import com.par.jbfh.training.dto.TrainingProgramResponse;
import com.par.jbfh.training.entity.TrainingProgram;
import com.par.jbfh.training.enums.LoadLevel;
import com.par.jbfh.training.enums.TrainingCycle;
import com.par.jbfh.training.repository.TrainingProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingProgramService {

    private final TrainingProgramRepository programRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public TrainingProgramResponse create(CreateTrainingProgramRequest request) {
        TrainingProgram program = new TrainingProgram();
        program.setBirthYear(request.getBirthYear());
        program.setLoadLevel(request.getLoadLevel());
        program.setCycle(request.getCycle());
        program.setPercentage(request.getPercentage());

        if (request.getClubId() != null) {
            Club club = clubRepository.findById(request.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
            program.setClub(club);
        }

        program = programRepository.save(program);
        log.info("Created training program (birthYear={}, loadLevel={}, cycle={})",
                program.getBirthYear(), program.getLoadLevel(), program.getCycle());
        return toResponse(program);
    }

    @Transactional(readOnly = true)
    public List<TrainingProgramResponse> getAll(Integer birthYear, LoadLevel loadLevel, TrainingCycle cycle) {
        List<TrainingProgram> programs;
        if (birthYear != null && loadLevel != null && cycle != null) {
            programs = programRepository.findByBirthYearAndLoadLevelAndCycle(birthYear, loadLevel, cycle);
        } else if (birthYear != null) {
            programs = programRepository.findByBirthYear(birthYear);
        } else if (loadLevel != null) {
            programs = programRepository.findByLoadLevel(loadLevel);
        } else if (cycle != null) {
            programs = programRepository.findByCycle(cycle);
        } else {
            programs = programRepository.findAll();
        }
        return programs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TrainingProgramResponse getById(UUID id) {
        TrainingProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training program not found: " + id));
        return toResponse(program);
    }

    @Transactional
    public TrainingProgramResponse update(UUID id, CreateTrainingProgramRequest request) {
        TrainingProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training program not found: " + id));

        if (request.getBirthYear() != null) program.setBirthYear(request.getBirthYear());
        if (request.getLoadLevel() != null) program.setLoadLevel(request.getLoadLevel());
        if (request.getCycle() != null) program.setCycle(request.getCycle());
        if (request.getPercentage() != null) program.setPercentage(request.getPercentage());

        program = programRepository.save(program);
        log.info("Updated training program (birthYear={}, loadLevel={}, cycle={})",
                program.getBirthYear(), program.getLoadLevel(), program.getCycle());
        return toResponse(program);
    }

    @Transactional
    public void delete(UUID id) {
        TrainingProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training program not found: " + id));
        programRepository.delete(program);
        log.info("Deleted training program (birthYear={}, loadLevel={}, cycle={})",
                program.getBirthYear(), program.getLoadLevel(), program.getCycle());
    }

    private TrainingProgramResponse toResponse(TrainingProgram program) {
        return new TrainingProgramResponse(
                program.getId(),
                program.getBirthYear(),
                program.getLoadLevel(),
                program.getCycle(),
                program.getPercentage(),
                program.getClub() != null ? program.getClub().getId() : null,
                program.getClub() != null ? program.getClub().getName() : null,
                program.getCreatedAt()
        );
    }
}