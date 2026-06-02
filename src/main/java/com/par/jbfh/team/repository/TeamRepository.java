package com.par.jbfh.team.repository;

import com.par.jbfh.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID>, JpaSpecificationExecutor<Team> {

    Page<Team> findByClubId(UUID clubId, Pageable pageable);

    Page<Team> findByClubIdAndActiveTrue(UUID clubId, Pageable pageable);

    Optional<Team> findByIdAndClubId(UUID id, UUID clubId);

    boolean existsByNameAndClubId(String name, UUID clubId);

    boolean existsByNameAndClubIdAndIdNot(String name, UUID clubId, UUID id);
}