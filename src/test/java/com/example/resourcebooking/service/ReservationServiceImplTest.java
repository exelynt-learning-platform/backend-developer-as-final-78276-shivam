package com.example.resourcebooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.resourcebooking.dto.ReservationRequestDto;
import com.example.resourcebooking.dto.ReservationResponseDto;
import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.exception.ConflictException;
import com.example.resourcebooking.exception.ForbiddenException;
import com.example.resourcebooking.exception.ReservationNotFoundException;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User user;
    private Resource resource;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole(Role.USER);

        resource = new Resource();
        resource.setId(10L);
        resource.setName("Meeting Room");
        resource.setDescription("Test Resource");
        resource.setPrice(new BigDecimal("500.00"));
        resource.setAvailable(true);

        setAuthentication("user");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(String username) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of());

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    // ---------------------------------------------------------
    // CREATE RESERVATION
    // ---------------------------------------------------------

    @Test
    void createReservation_shouldCreateSuccessfully() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(2);

        LocalDateTime end =
                start.plusHours(2);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        null);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository
                .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(resource),
                        anyCollection(),
                        eq(end),
                        eq(start)))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {

                    Reservation reservation =
                            invocation.getArgument(0);

                    reservation.setId(100L);

                    return reservation;
                });

        ReservationResponseDto result =
                reservationService.createReservation(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getResourceId());
        assertEquals("Meeting Room", result.getResourceName());
        assertEquals(
                new BigDecimal("500.00"),
                result.getPrice());
        assertEquals(
                ReservationStatus.PENDING,
                result.getStatus());

        verify(reservationRepository)
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldRejectPastStartTime() {

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        LocalDateTime.now().minusHours(2),
                        LocalDateTime.now().plusHours(1),
                        null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .createReservation(request));

        assertEquals(
                "Start time cannot be in the past",
                exception.getMessage());
    }

    @Test
    void createReservation_shouldRejectWhenStartIsAfterEnd() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(3);

        LocalDateTime end =
                start.minusHours(1);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .createReservation(request));

        assertEquals(
                "Start time must be before end time",
                exception.getMessage());
    }

    @Test
    void createReservation_shouldRejectOverlappingReservation() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(2);

        LocalDateTime end =
                start.plusHours(2);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        null);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository
                .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(resource),
                        anyCollection(),
                        eq(end),
                        eq(start)))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> reservationService
                                .createReservation(request));

        assertEquals(
                "Resource is already booked for the selected time",
                exception.getMessage());

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldRejectUnavailableResource() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(2);

        LocalDateTime end =
                start.plusHours(1);

        resource.setAvailable(false);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        null);

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(resource));

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> reservationService
                                .createReservation(request));

        assertEquals(
                "Resource is not available",
                exception.getMessage());
    }

    // ---------------------------------------------------------
    // GET RESERVATION BY ID
    // ---------------------------------------------------------

    @Test
    void getReservationById_shouldAllowAdmin() {

        Reservation reservation =
                createReservation(
                        100L,
                        user,
                        resource,
                        ReservationStatus.PENDING);

        User admin = new User();
        admin.setId(99L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        setAuthentication("admin");

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        ReservationResponseDto result =
                reservationService.getReservationById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(
                ReservationStatus.PENDING,
                result.getStatus());
    }

    @Test
    void getReservationById_shouldAllowOwner() {

        Reservation reservation =
                createReservation(
                        100L,
                        user,
                        resource,
                        ReservationStatus.PENDING);

        setAuthentication("user");

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        ReservationResponseDto result =
                reservationService.getReservationById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void getReservationById_shouldRejectOtherUser() {

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setRole(Role.USER);

        Reservation reservation =
                createReservation(
                        100L,
                        user,
                        resource,
                        ReservationStatus.PENDING);

        setAuthentication("other");

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(userRepository.findByUsername("other"))
                .thenReturn(Optional.of(otherUser));

        ForbiddenException exception =
                assertThrows(
                        ForbiddenException.class,
                        () -> reservationService
                                .getReservationById(100L));

        assertEquals(
                "You are not allowed to access this reservation",
                exception.getMessage());
    }

    @Test
    void getReservationById_shouldRejectMissingReservation() {

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReservationNotFoundException exception =
                assertThrows(
                        ReservationNotFoundException.class,
                        () -> reservationService
                                .getReservationById(999L));

        assertEquals(
                "Reservation not found",
                exception.getMessage());
    }

    // ---------------------------------------------------------
    // UPDATE RESERVATION
    // ---------------------------------------------------------

    @Test
    void updateReservation_shouldAllowChangingToCancelled() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(5);

        LocalDateTime end =
                start.plusHours(2);

        Reservation reservation =
                createReservation(
                        100L,
                        user,
                        resource,
                        ReservationStatus.PENDING);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        ReservationStatus.CANCELLED);

        when(reservationRepository
                .findByIdForUpdate(100L))
                .thenReturn(Optional.of(reservation));

        when(resourceRepository
                .findByIdForUpdate(10L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        ReservationResponseDto result =
                reservationService.updateReservation(
                        100L,
                        request);

        assertNotNull(result);

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus());

        verify(reservationRepository, never())
                .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        any(),
                        anyCollection(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void updateReservation_shouldRejectOverlapForActiveStatus() {

        LocalDateTime start =
                LocalDateTime.now().plusHours(5);

        LocalDateTime end =
                start.plusHours(2);

        Reservation reservation =
                createReservation(
                        100L,
                        user,
                        resource,
                        ReservationStatus.PENDING);

        ReservationRequestDto request =
                new ReservationRequestDto(
                        10L,
                        start,
                        end,
                        ReservationStatus.CONFIRMED);

        when(reservationRepository
                .findByIdForUpdate(100L))
                .thenReturn(Optional.of(reservation));

        when(resourceRepository
                .findByIdForUpdate(10L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository
                .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        eq(resource),
                        anyCollection(),
                        eq(end),
                        eq(start),
                        eq(100L)))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> reservationService
                                .updateReservation(
                                        100L,
                                        request));

        assertEquals(
                "Resource is already booked for the selected time",
                exception.getMessage());

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    void deleteReservation_shouldDeleteExistingReservation() {

        Reservation reservation =
                new Reservation();

        when(reservationRepository
                .findByIdForUpdate(100L))
                .thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(100L);

        verify(reservationRepository)
                .delete(reservation);
    }

    // ---------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------

    @Test
    void getAllReservations_shouldRejectNegativePage() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .getAllReservations(
                                        null,
                                        null,
                                        null,
                                        -1,
                                        10,
                                        null));

        assertEquals(
                "Page must be 0 or greater",
                exception.getMessage());
    }

    @Test
    void getAllReservations_shouldRejectInvalidSize() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .getAllReservations(
                                        null,
                                        null,
                                        null,
                                        0,
                                        101,
                                        null));

        assertEquals(
                "Size must be between 1 and 100",
                exception.getMessage());
    }

    @Test
    void getAllReservations_shouldRejectInvalidSortField() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .getAllReservations(
                                        null,
                                        null,
                                        null,
                                        0,
                                        10,
                                        "username,asc"));

        assertEquals(
                "Invalid sort field. Allowed: id,startTime,endTime,price,status",
                exception.getMessage());
    }

    @Test
    void getAllReservations_shouldRejectInvalidSortDirection() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .getAllReservations(
                                        null,
                                        null,
                                        null,
                                        0,
                                        10,
                                        "price,wrong"));

        assertEquals(
                "Sort direction must be asc or desc",
                exception.getMessage());
    }

    @Test
    void getAllReservations_shouldRejectInvalidPriceRange() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService
                                .getAllReservations(
                                        null,
                                        new BigDecimal("1000"),
                                        new BigDecimal("500"),
                                        0,
                                        10,
                                        null));

        assertEquals(
                "minPrice cannot be greater than maxPrice",
                exception.getMessage());
    }

    // ---------------------------------------------------------
    // HELPER
    // ---------------------------------------------------------

    private Reservation createReservation(
            Long id,
            User reservationUser,
            Resource reservationResource,
            ReservationStatus status) {

        LocalDateTime start =
                LocalDateTime.now().plusHours(2);

        LocalDateTime end =
                start.plusHours(2);

        Reservation reservation =
                new Reservation();

        reservation.setId(id);
        reservation.setUser(reservationUser);
        reservation.setResource(reservationResource);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setPrice(
                new BigDecimal("500.00"));
        reservation.setStatus(status);

        return reservation;
    }
}