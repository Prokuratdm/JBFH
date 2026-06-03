package com.par.jbfh.location.service;

import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.location.dto.CreateLocationRequest;
import com.par.jbfh.location.dto.LocationResponse;
import com.par.jbfh.location.dto.UpdateLocationRequest;
import com.par.jbfh.location.entity.Location;
import com.par.jbfh.location.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private LocationRepository locationRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private UserRepository userRepository;

    private LocationService locationService;

    private UUID clubId;
    private Club club;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository, clubRepository, userRepository);

        clubId = UUID.randomUUID();
        club = new Club("Test Club");
        club.setId(clubId);
    }

    @Test
    void createShouldSucceed() {
        CreateLocationRequest request = new CreateLocationRequest();
        request.setName("Arena 1");

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(locationRepository.existsByNameAndClubId("Arena 1", clubId)).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        LocationResponse response = locationService.create(clubId, request);

        assertThat(response.name()).isEqualTo("Arena 1");
        assertThat(response.active()).isTrue();
        assertThat(response.clubId()).isEqualTo(clubId);
    }

    @Test
    void createShouldThrowWhenClubNotFound() {
        CreateLocationRequest request = new CreateLocationRequest();
        request.setName("Arena");

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.create(clubId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + clubId);
    }

    @Test
    void createShouldThrowWhenNameExistsInClub() {
        CreateLocationRequest request = new CreateLocationRequest();
        request.setName("Arena");

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(locationRepository.existsByNameAndClubId("Arena", clubId)).thenReturn(true);

        assertThatThrownBy(() -> locationService.create(clubId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location already exists with name 'Arena' in this club");
    }

    @Test
    void getAllShouldReturnActiveOnly() {
        Location loc = new Location();
        loc.setId(UUID.randomUUID());
        loc.setName("Arena");
        loc.setClub(club);
        loc.setActive(true);

        when(locationRepository.findByClubIdAndActiveTrue(clubId)).thenReturn(List.of(loc));

        List<LocationResponse> result = locationService.getAll(clubId, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Arena");
    }

    @Test
    void getAllShouldIncludeInactiveWhenRequested() {
        Location loc = new Location();
        loc.setId(UUID.randomUUID());
        loc.setName("Arena");
        loc.setClub(club);
        loc.setActive(false);

        when(locationRepository.findByClubId(clubId)).thenReturn(List.of(loc));

        List<LocationResponse> result = locationService.getAll(clubId, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).active()).isFalse();
    }

    @Test
    void getByIdShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        Location loc = new Location();
        loc.setId(id);
        loc.setName("Arena");
        loc.setClub(club);
        loc.setActive(true);

        when(locationRepository.findByIdAndClubId(id, clubId)).thenReturn(Optional.of(loc));

        LocationResponse response = locationService.getById(clubId, id);

        assertThat(response.name()).isEqualTo("Arena");
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findByIdAndClubId(id, clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getById(clubId, id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location not found: " + id + " in club: " + clubId);
    }

    @Test
    void updateShouldChangeName() {
        UUID id = UUID.randomUUID();
        Location loc = new Location();
        loc.setId(id);
        loc.setName("Old");
        loc.setClub(club);

        when(locationRepository.findByIdAndClubId(id, clubId)).thenReturn(Optional.of(loc));
        when(locationRepository.save(any())).thenReturn(loc);

        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setName("New");

        LocationResponse response = locationService.update(clubId, id, request);

        assertThat(response.name()).isEqualTo("New");
    }

    @Test
    void setActiveShouldDeactivate() {
        UUID id = UUID.randomUUID();
        Location loc = new Location();
        loc.setId(id);
        loc.setName("Arena");
        loc.setClub(club);
        loc.setActive(true);

        when(locationRepository.findByIdAndClubId(id, clubId)).thenReturn(Optional.of(loc));
        when(locationRepository.save(any())).thenReturn(loc);

        LocationResponse response = locationService.setActive(clubId, id, false);

        assertThat(response.active()).isFalse();
    }
}