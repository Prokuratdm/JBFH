package com.par.jbfh.auth.repository;

import com.par.jbfh.auth.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, UUID> {
    boolean existsByName(String name);
}
