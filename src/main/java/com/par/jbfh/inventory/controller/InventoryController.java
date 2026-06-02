package com.par.jbfh.inventory.controller;

import com.par.jbfh.inventory.dto.CreateInventoryRequest;
import com.par.jbfh.inventory.dto.InventoryResponse;
import com.par.jbfh.inventory.dto.UpdateInventoryRequest;
import com.par.jbfh.inventory.service.InventoryService;
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
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management API")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Create inventory", description = "Create a new inventory item. Admin/methodist may omit clubId for global inventory.")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@Valid @RequestBody CreateInventoryRequest request) {
        return inventoryService.createInventory(request);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all inventory", description = "Returns paginated list. Admins/methodists see all; others see global + their club's.")
    public Page<InventoryResponse> getAll(
            @RequestParam(defaultValue = "true") boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        return inventoryService.getAll(active, pageable);
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get inventory by ID")
    public InventoryResponse getById(@PathVariable UUID id) {
        return inventoryService.getById(id);
    }

    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Update inventory")
    public InventoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateInventoryRequest request) {
        return inventoryService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST"})
    @Operation(summary = "Activate or deactivate inventory")
    public InventoryResponse setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return inventoryService.setActive(id, active);
    }
}