package com.par.jbfh.team.repository;

import com.par.jbfh.team.entity.TeamCoach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamCoachRepository extends JpaRepository<TeamCoach, UUID> {

    List<TeamCoach> findByTeamId(UUID teamId);

    Optional<TeamCoach> findByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

    List<TeamCoach> findByUserId(UUID userId);
}