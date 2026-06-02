package com.par.jbfh.club.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.club.dto.ClubResponse;
import com.par.jbfh.club.dto.CreateClubRequest;
import com.par.jbfh.club.dto.UpdateClubRequest;
import com.par.jbfh.storage.FileStorage;
import com.par.jbfh.storage.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;
    @Mock
    private FileStorage fileStorage;

    private ClubService clubService;

    private Club club;

    @BeforeEach
    void setUp() {
        clubService = new ClubService(clubRepository, fileStorage);

        club = new Club("Hockey Club Minsk");
        club.setId(UUID.randomUUID());
        club.setAddress("Minsk, ul. Pobediteley 20");
        club.setDescription("Best hockey school");
    }

    // --- Create Club ---

    @Test
    void createClub_shouldCreateSuccessfully() {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("New Club");
        request.setAddress("Address");
        request.setDescription("Description");

        when(clubRepository.existsByName("New Club")).thenReturn(false);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> {
            Club c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ClubResponse response = clubService.createClub(request);

        assertEquals("New Club", response.getName());
        assertEquals("Address", response.getAddress());
        assertEquals("Description", response.getDescription());
        assertNotNull(response.getId());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void createClub_shouldThrowWhenNameExists() {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("Existing Club");

        when(clubRepository.existsByName("Existing Club")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> clubService.createClub(request));
        verify(clubRepository, never()).save(any());
    }

    // --- Get All Clubs ---

    @Test
    void getAllClubs_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(clubRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(club)));

        Page<ClubResponse> result = clubService.getAllClubs(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Hockey Club Minsk", result.getContent().getFirst().getName());
    }

    // --- Get Club By Id ---

    @Test
    void getClubById_shouldReturnClub() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));

        ClubResponse response = clubService.getClubById(club.getId());

        assertEquals(club.getId(), response.getId());
        assertEquals("Hockey Club Minsk", response.getName());
    }

    @Test
    void getClubById_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clubService.getClubById(id));
    }

    // --- Update Club ---

    @Test
    void updateClub_shouldUpdateAddressAndDescription() {
        UpdateClubRequest request = new UpdateClubRequest();
        request.setAddress("New Address");
        request.setDescription("New Description");

        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubResponse response = clubService.updateClub(club.getId(), request);

        assertEquals("New Address", response.getAddress());
        assertEquals("New Description", response.getDescription());
        // Name should remain unchanged
        assertEquals("Hockey Club Minsk", response.getName());
    }

    @Test
    void updateClub_shouldUpdateOnlyAddressWhenDescriptionNull() {
        club.setDescription("Original Description");

        UpdateClubRequest request = new UpdateClubRequest();
        request.setAddress("New Address");

        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubResponse response = clubService.updateClub(club.getId(), request);

        assertEquals("New Address", response.getAddress());
        assertEquals("Original Description", response.getDescription());
    }

    @Test
    void updateClub_shouldThrowWhenClubNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clubService.updateClub(id, new UpdateClubRequest()));
    }

    // --- Upload Logo ---

    @Test
    void uploadLogo_shouldUploadSuccessfully() {
        MultipartFile file = mock(MultipartFile.class);
        String savedPath = "uploads/logos/" + club.getId() + "_1234567890.jpg";

        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(fileStorage.save(file, club.getId(), FileType.CLUB_LOGO)).thenReturn(savedPath);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubResponse response = clubService.uploadLogo(club.getId(), file);

        assertNotNull(response.getLogoUrl());
        assertTrue(response.getLogoUrl().contains(club.getId().toString()));
        verify(fileStorage).save(file, club.getId(), FileType.CLUB_LOGO);
    }

    @Test
    void uploadLogo_shouldDeleteOldLogoWhenExists() {
        MultipartFile file = mock(MultipartFile.class);
        club.setLogoPath("uploads/logos/old_logo.jpg");

        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(fileStorage.save(file, club.getId(), FileType.CLUB_LOGO)).thenReturn("uploads/logos/new_logo.jpg");
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        clubService.uploadLogo(club.getId(), file);

        verify(fileStorage).delete("uploads/logos/old_logo.jpg");
        verify(fileStorage).save(file, club.getId(), FileType.CLUB_LOGO);
    }

    @Test
    void uploadLogo_shouldThrowWhenClubNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clubService.uploadLogo(id, mock(MultipartFile.class)));
    }

    // --- Get Logo ---

    @Test
    void getLogo_shouldReturnResource() {
        club.setLogoPath("uploads/logos/logo.jpg");
        Resource resource = mock(Resource.class);

        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(fileStorage.getResource("uploads/logos/logo.jpg")).thenReturn(resource);

        Resource result = clubService.getLogo(club.getId());

        assertEquals(resource, result);
    }

    @Test
    void getLogo_shouldThrowWhenNoLogoPath() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));

        assertThrows(IllegalArgumentException.class, () -> clubService.getLogo(club.getId()));
    }

    @Test
    void getLogo_shouldThrowWhenClubNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clubService.getLogo(id));
    }
}