package com.example.resourcebooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.exception.UnauthorizedException;
import com.example.resourcebooking.exception.UserNotFoundException;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final List<ReservationStatus> BLOCKING_STATUSES =
            List.of(
                    ReservationStatus.PENDING,
                    ReservationStatus.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ReservationResponseDto createReservation(
            ReservationRequestDto request) {

        validateReservationTime(request);

        User user = getLoggedInUser();

        Resource resource = resourceRepository
                .findByIdForUpdate(request.getResourceId())
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
                            BLOCKING_STATUSES,
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
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long id) {

        Reservation reservation = findReservation(id);
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
    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getMyReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        User user = getLoggedInUser();

        validatePageAndPrice(
                page,
                size,
                minPrice,
                maxPrice);

        Pageable pageable =
                createPageable(page, size, sort);

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
    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {

        validatePageAndPrice(
                page,
                size,
                minPrice,
                maxPrice);

        Pageable pageable =
                createPageable(page, size, sort);

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
    @Transactional
    public ReservationResponseDto updateReservation(
            Long id,
            ReservationRequestDto request) {

        validateReservationTime(request);

        Reservation reservation =
                reservationRepository.findByIdForUpdate(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        Resource resource = resourceRepository
                .findByIdForUpdate(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new ConflictException(
                    "Resource is not available");
        }

        ReservationStatus targetStatus =
                request.getStatus() != null
                        ? request.getStatus()
                        : reservation.getStatus();

        /*
         * Only active reservations block other bookings.
         * A reservation being changed to CANCELLED is non-blocking.
         */
        if (targetStatus != ReservationStatus.CANCELLED) {

            boolean overlapping =
                    reservationRepository
                        .existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                                resource,
                                BLOCKING_STATUSES,
                                request.getEndTime(),
                                request.getStartTime(),
                                id);

            if (overlapping) {
                throw new ConflictException(
                        "Resource is already booked for the selected time");
            }
        }

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(resource.getPrice());
        reservation.setStatus(targetStatus);

        return convertToResponseDto(
                reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {

        Reservation reservation =
                reservationRepository.findByIdForUpdate(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

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
                        new UserNotFoundException(
                                "User not found"));
    }

    private String getLoggedInUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(
                        authentication.getName())) {

            throw new UnauthorizedException(
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

        if (request.getStartTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Start time cannot be in the past");
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

        if (sortData.length > 2) {
            throw new IllegalArgumentException(
                    "Sort format must be field,direction");
        }

        String field = sortData[0].trim();

        if (!isAllowedSortField(field)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed: "
                            + "id,startTime,endTime,price,status");
        }

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortData.length == 2) {

            String directionValue =
                    sortData[1].trim();

            if ("desc".equalsIgnoreCase(directionValue)) {
                direction = Sort.Direction.DESC;

            } else if (!"asc".equalsIgnoreCase(directionValue)) {
                throw new IllegalArgumentException(
                        "Sort direction must be asc or desc");
            }
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
