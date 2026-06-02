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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private FileStorage fileStorage;

    private ClubService clubService;

    @BeforeEach
    void setUp() {
        clubService = new ClubService(clubRepository, fileStorage);
    }

    @Test
    void createClubShouldSucceed() {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("HC Test");
        request.setAddress("Minsk");
        request.setDescription("Description");

        when(clubRepository.existsByName("HC Test")).thenReturn(false);
        when(clubRepository.save(any(Club.class))).thenAnswer(inv -> {
            Club c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ClubResponse response = clubService.createClub(request);

        assertThat(response.getName()).isEqualTo("HC Test");
        assertThat(response.getAddress()).isEqualTo("Minsk");
        assertThat(response.getDescription()).isEqualTo("Description");
    }

    @Test
    void createClubShouldThrowWhenNameExists() {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("HC Test");

        when(clubRepository.existsByName("HC Test")).thenReturn(true);

        assertThatThrownBy(() -> clubService.createClub(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club already exists with name: HC Test");
    }

    @Test
    void getAllClubsShouldReturnPage() {
        Club club = new Club("HC");
        club.setId(UUID.randomUUID());
        Page<Club> page = new PageImpl<>(List.of(club));

        when(clubRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ClubResponse> result = clubService.getAllClubs(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("HC");
    }

    @Test
    void getClubByIdShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));

        ClubResponse response = clubService.getClubById(id);

        assertThat(response.getName()).isEqualTo("HC");
    }

    @Test
    void getClubByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.getClubById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + id);
    }

    @Test
    void updateClubShouldUpdateFields() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);
        club.setAddress("old address");
        club.setDescription("old desc");

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        UpdateClubRequest request = new UpdateClubRequest();
        request.setAddress("new address");
        request.setDescription("new desc");

        ClubResponse response = clubService.updateClub(id, request);

        assertThat(response.getAddress()).isEqualTo("new address");
        assertThat(response.getDescription()).isEqualTo("new desc");
    }

    @Test
    void updateClubShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.updateClub(id, new UpdateClubRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + id);
    }

    @Test
    void uploadLogoShouldSaveAndReturnUpdated() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));
        MultipartFile file = mock(MultipartFile.class);
        when(fileStorage.save(file, id, FileType.CLUB_LOGO)).thenReturn("uploads/logos/logo.png");
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        ClubResponse response = clubService.uploadLogo(id, file);

        assertThat(response.getLogoUrl()).isEqualTo("/api/v1/clubs/" + id + "/logo");
    }

    @Test
    void uploadLogoShouldDeleteOldLogoWhenExists() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);
        club.setLogoPath("old/path.png");

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));
        MultipartFile file = mock(MultipartFile.class);
        when(fileStorage.save(file, id, FileType.CLUB_LOGO)).thenReturn("new/path.png");
        when(clubRepository.save(any(Club.class))).thenReturn(club);

        clubService.uploadLogo(id, file);

        verify(fileStorage).delete("old/path.png");
    }

    @Test
    void uploadLogoShouldThrowWhenClubNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.uploadLogo(id, mock(MultipartFile.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + id);
    }

    @Test
    void getLogoShouldReturnResource() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);
        club.setLogoPath("logos/test.png");

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));
        Resource resource = new ByteArrayResource("test".getBytes());
        when(fileStorage.getResource("logos/test.png")).thenReturn(resource);

        Resource result = clubService.getLogo(id);

        assertThat(result).isNotNull();
    }

    @Test
    void getLogoShouldThrowWhenNoLogo() {
        UUID id = UUID.randomUUID();
        Club club = new Club("HC");
        club.setId(id);

        when(clubRepository.findById(id)).thenReturn(Optional.of(club));

        assertThatThrownBy(() -> clubService.getLogo(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club has no logo: " + id);
    }

    @Test
    void getLogoShouldThrowWhenClubNotFound() {
        UUID id = UUID.randomUUID();
        when(clubRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.getLogo(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + id);
    }
}