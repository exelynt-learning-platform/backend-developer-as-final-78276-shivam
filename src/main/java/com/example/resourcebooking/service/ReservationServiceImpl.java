package com.example.resourcebooking.service;

import java.math.BigDecimal;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.resourcebooking.dto.ReservationRequestDto;
import com.example.resourcebooking.dto.ReservationResponseDto;
import com.example.resourcebooking.entity.*;
import com.example.resourcebooking.exception.*;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public ReservationResponseDto createReservation(
            ReservationRequestDto request) {

        validateReservationTime(request);

        User user = getLoggedInUser();

        Resource resource = resourceRepository.findById(
                request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new ConflictException(
                    "Resource is not available");
        }

        boolean overlapping =
                reservationRepository
                    .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                            resource,
                            java.util.List.of(
                                    ReservationStatus.PENDING,
                                    ReservationStatus.CONFIRMED),
                            request.getEndTime(),
                            request.getStartTime());

        if (overlapping) {
            throw new ConflictException(
                    "Resource is already booked for the selected time");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(resource.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);

        return convertToResponseDto(
                reservationRepository.save(reservation));
    }

    @Override
    public ReservationResponseDto getReservationById(Long id) {

        Reservation reservation =
                findReservation(id);

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() == Role.ADMIN) {
            return convertToResponseDto(reservation);
        }

        if (!reservation.getUser().getId()
                .equals(loggedInUser.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to access this reservation");
        }

        return convertToResponseDto(reservation);
    }

    @Override
    public Page<ReservationResponseDto> getMyReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        User user = getLoggedInUser();

        validatePageAndPrice(page, size, minPrice, maxPrice);

        Pageable pageable = createPageable(page, size, sort);

        Specification<Reservation> specification =
                buildSpecification(
                        user,
                        status,
                        minPrice,
                        maxPrice);

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    public Page<ReservationResponseDto> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        validatePageAndPrice(page, size, minPrice, maxPrice);

        Pageable pageable = createPageable(page, size, sort);

        Specification<Reservation> specification =
                buildSpecification(
                        null,
                        status,
                        minPrice,
                        maxPrice);

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::convertToResponseDto);
    }

    @Override
    public ReservationResponseDto updateReservation(
            Long id,
            ReservationRequestDto request) {

        validateReservationTime(request);

        Reservation reservation =
                findReservation(id);

        Resource resource = resourceRepository.findById(
                request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new ConflictException(
                    "Resource is not available");
        }

        boolean overlapping =
                reservationRepository
                    .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                            resource,
                            java.util.List.of(
                                    ReservationStatus.PENDING,
                                    ReservationStatus.CONFIRMED),
                            request.getEndTime(),
                            request.getStartTime(),
                            id);

        if (reservation.getStatus() != ReservationStatus.CANCELLED
                && overlapping) {

            throw new ConflictException(
                    "Resource is already booked for the selected time");
        }

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(resource.getPrice());

        // ADMIN may update reservation status.
        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        return convertToResponseDto(
                reservationRepository.save(reservation));
    }

    @Override
    public void deleteReservation(Long id) {

        Reservation reservation =
                findReservation(id);

        reservationRepository.delete(reservation);
    }

    private Reservation findReservation(Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation not found"));
    }

    private User getLoggedInUser() {

        String username = getLoggedInUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));
    }

    private String getLoggedInUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getName())) {

            throw new ForbiddenException(
                    "Authentication is required");
        }

        return authentication.getName();
    }

    private void validateReservationTime(
            ReservationRequestDto request) {

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Start time is required");
        }

        if (request.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "End time is required");
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new IllegalArgumentException(
                    "Start time must be before end time");
        }
    }

    private void validatePageAndPrice(
            int page,
            int size,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100");
        }

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "minPrice must be zero or greater");
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "maxPrice must be zero or greater");
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice cannot be greater than maxPrice");
        }
    }

    private Pageable createPageable(
            int page,
            int size,
            String sort) {

        if (sort == null || sort.isBlank()) {
            return PageRequest.of(
                    page,
                    size,
                    Sort.by("id").ascending());
        }

        String[] sortData = sort.split(",");

        String field = sortData[0].trim();

        if (!isAllowedSortField(field)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed: id,startTime,endTime,price,status");
        }

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortData.length > 1
                && "desc".equalsIgnoreCase(
                        sortData[1].trim())) {

            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, field));
    }

    private boolean isAllowedSortField(String field) {

        return field.equals("id")
                || field.equals("startTime")
                || field.equals("endTime")
                || field.equals("price")
                || field.equals("status");
    }

    private Specification<Reservation> buildSpecification(
            User user,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        Specification<Reservation> specification =
                (root, query, cb) -> cb.conjunction();

        if (user != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("user"), user));
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("status"), status));
        }

        if (minPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.<BigDecimal>get("price"),
                                    minPrice));
        }

        if (maxPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.<BigDecimal>get("price"),
                                    maxPrice));
        }

        return specification;
    }

    private ReservationResponseDto convertToResponseDto(
            Reservation reservation) {

        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getResource().getId(),
                reservation.getUser().getUsername(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getResource().getDescription());
    }
}
