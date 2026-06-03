package com.par.jbfh.location.repository;

import com.par.jbfh.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByClubIdAndActiveTrue(UUID clubId);

    List<Location> findByClubId(UUID clubId);

    Optional<Location> findByIdAndClubId(UUID id, UUID clubId);

    boolean existsByNameAndClubId(String name, UUID clubId);

    boolean existsByNameAndClubIdAndIdNot(String name, UUID clubId, UUID id);
}