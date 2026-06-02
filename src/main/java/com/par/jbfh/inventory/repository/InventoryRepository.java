package com.par.jbfh.inventory.repository;

import com.par.jbfh.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @Query("SELECT i FROM Inventory i WHERE i.active = true AND (i.club IS NULL OR i.club.id = :clubId)")
    Page<Inventory> findVisibleForClub(@Param("clubId") UUID clubId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.club IS NULL OR i.club.id = :clubId")
    Page<Inventory> findAllVisibleForClub(@Param("clubId") UUID clubId, Pageable pageable);

    Page<Inventory> findByActiveTrue(Pageable pageable);
}