package com.par.jbfh.inventory.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.inventory.dto.CreateInventoryRequest;
import com.par.jbfh.inventory.dto.InventoryResponse;
import com.par.jbfh.inventory.dto.UpdateInventoryRequest;
import com.par.jbfh.inventory.entity.Inventory;
import com.par.jbfh.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdminOrMethodist = hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_METHODIST");

        Inventory inventory = new Inventory();
        inventory.setName(request.getName());
        inventory.setActive(true);

        if (isAdminOrMethodist) {
            // Admin/methodist may optionally specify clubId
            if (request.getClubId() != null) {
                Club club = clubRepository.findById(request.getClubId())
                        .orElseThrow(() -> new IllegalArgumentException("Club not found: " + request.getClubId()));
                inventory.setClub(club);
            }
            // else: club stays null → global inventory
        } else {
            // Club-bound users always assign to their club
            if (currentUser.getClub() == null) {
                throw new IllegalArgumentException("Club is required for non-admin users");
            }
            inventory.setClub(currentUser.getClub());
        }

        inventory = inventoryRepository.save(inventory);
        log.info("Created inventory '{}'", inventory.getName());

        return toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getAll(boolean activeOnly, Pageable pageable) {
        User currentUser = getCurrentUser();
        boolean isAdminOrMethodist = hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_METHODIST");

        Page<Inventory> page;
        if (isAdminOrMethodist) {
            page = activeOnly
                    ? inventoryRepository.findByActiveTrue(pageable)
                    : inventoryRepository.findAll(pageable);
        } else {
            UUID clubId = currentUser.getClub() != null ? currentUser.getClub().getId() : null;
            if (clubId == null) {
                return Page.empty(pageable);
            }
            page = activeOnly
                    ? inventoryRepository.findVisibleForClub(clubId, pageable)
                    : inventoryRepository.findAllVisibleForClub(clubId, pageable);
        }

        return page.map(this::toInventoryResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + id));
        return toInventoryResponse(inventory);
    }

    @Transactional
    public InventoryResponse update(UUID id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + id));

        if (request.getName() != null) {
            inventory.setName(request.getName());
        }

        inventory = inventoryRepository.save(inventory);
        log.info("Updated inventory '{}'", inventory.getName());

        return toInventoryResponse(inventory);
    }

    @Transactional
    public InventoryResponse setActive(UUID id, boolean active) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + id));

        inventory.setActive(active);
        inventory = inventoryRepository.save(inventory);

        log.info("Inventory '{}' active status set to {}", inventory.getName(), active);

        return toInventoryResponse(inventory);
    }

    private InventoryResponse toInventoryResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getName(),
                inventory.isActive(),
                inventory.getClub() != null ? inventory.getClub().getId() : null,
                inventory.getClub() != null ? inventory.getClub().getName() : null,
                inventory.getCreatedAt()
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUserId()));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
    }
}