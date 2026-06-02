package com.par.jbfh.inventory.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import com.par.jbfh.inventory.dto.CreateInventoryRequest;
import com.par.jbfh.inventory.dto.InventoryResponse;
import com.par.jbfh.inventory.dto.UpdateInventoryRequest;
import com.par.jbfh.inventory.entity.Inventory;
import com.par.jbfh.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private InventoryService inventoryService;

    private UUID clubId;
    private UUID userId;
    private Club club;
    private User currentUser;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryRepository, clubRepository, userRepository);

        clubId = UUID.randomUUID();
        userId = UUID.randomUUID();
        club = new Club("Test Club");
        club.setId(clubId);

        currentUser = new User();
        currentUser.setId(userId);
        currentUser.setUsername("testuser");
        currentUser.setClub(club);
        currentUser.setRoles(Set.of(new Role("ROLE_CLUB")));

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        UserPrincipal principal = new UserPrincipal(user.getUsername(), user.getId(), List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void createInventoryShouldSucceedForClubUser() {
        mockAuthenticatedUser(currentUser);

        CreateInventoryRequest request = new CreateInventoryRequest();
        request.setName("Шайба");

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> {
            Inventory i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        InventoryResponse response = inventoryService.createInventory(request);

        assertThat(response.name()).isEqualTo("Шайба");
        assertThat(response.active()).isTrue();
        assertThat(response.clubId()).isEqualTo(clubId);
    }

    @Test
    void createInventoryShouldCreateGlobalForAdmin() {
        currentUser.setRoles(Set.of(new Role("ROLE_ADMIN")));
        currentUser.setClub(null);
        mockAuthenticatedUser(currentUser);

        CreateInventoryRequest request = new CreateInventoryRequest();
        request.setName("Global Item");

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> {
            Inventory i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        InventoryResponse response = inventoryService.createInventory(request);

        assertThat(response.name()).isEqualTo("Global Item");
        assertThat(response.clubId()).isNull();
    }

    @Test
    void createInventoryShouldThrowWhenClubUserHasNoClub() {
        currentUser.setClub(null);
        mockAuthenticatedUser(currentUser);

        CreateInventoryRequest request = new CreateInventoryRequest();
        request.setName("Item");

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club is required for non-admin users");
    }

    @Test
    void getAllShouldReturnPageForAdmin() {
        currentUser.setRoles(Set.of(new Role("ROLE_ADMIN")));
        currentUser.setClub(null);
        mockAuthenticatedUser(currentUser);

        Inventory inv = new Inventory();
        inv.setId(UUID.randomUUID());
        inv.setName("Item");
        inv.setActive(true);
        Page<Inventory> page = new PageImpl<>(List.of(inv));

        when(inventoryRepository.findByActiveTrue(any())).thenReturn(page);

        Page<InventoryResponse> result = inventoryService.getAll(true, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getByIdShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        Inventory inv = new Inventory();
        inv.setId(id);
        inv.setName("Item");
        inv.setActive(true);

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(inv));

        InventoryResponse response = inventoryService.getById(id);

        assertThat(response.name()).isEqualTo("Item");
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inventory not found: " + id);
    }

    @Test
    void updateShouldChangeName() {
        UUID id = UUID.randomUUID();
        Inventory inv = new Inventory();
        inv.setId(id);
        inv.setName("Old");

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any())).thenReturn(inv);

        UpdateInventoryRequest request = new UpdateInventoryRequest();
        request.setName("New");

        InventoryResponse response = inventoryService.update(id, request);

        assertThat(response.name()).isEqualTo("New");
    }

    @Test
    void setActiveShouldDeactivate() {
        UUID id = UUID.randomUUID();
        Inventory inv = new Inventory();
        inv.setId(id);
        inv.setName("Item");
        inv.setActive(true);

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any())).thenReturn(inv);

        InventoryResponse response = inventoryService.setActive(id, false);

        assertThat(response.active()).isFalse();
    }
}