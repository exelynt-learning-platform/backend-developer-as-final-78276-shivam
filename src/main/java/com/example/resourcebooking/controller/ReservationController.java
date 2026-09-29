package com.example.resourcebooking.controller;

import java.math.BigDecimal;

import com.example.resourcebooking.dto.ReservationRequestDto;
import com.example.resourcebooking.dto.ReservationResponseDto;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // USER + ADMIN
    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @Valid @RequestBody ReservationRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                    reservationService.createReservation(request)
                );
    }

    // USER - own reservations
    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponseDto>>
            getMyReservations(
                @RequestParam(required = false)
                ReservationStatus status,

                @RequestParam(required = false)
                BigDecimal minPrice,

                @RequestParam(required = false)
                BigDecimal maxPrice,

                @RequestParam(defaultValue = "0")
                int page,

                @RequestParam(defaultValue = "10")
                int size,

                @RequestParam(defaultValue = "id,asc")
                String sort) {

        return ResponseEntity.ok(
                reservationService.getMyReservations(
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sort)
        );
    }

    // ADMIN - all reservations
    @GetMapping
    public ResponseEntity<Page<ReservationResponseDto>>
            getAllReservations(
                @RequestParam(required = false)
                ReservationStatus status,

                @RequestParam(required = false)
                BigDecimal minPrice,

                @RequestParam(required = false)
                BigDecimal maxPrice,

                @RequestParam(defaultValue = "0")
                int page,

                @RequestParam(defaultValue = "10")
                int size,

                @RequestParam(defaultValue = "id,asc")
                String sort) {

        return ResponseEntity.ok(
                reservationService.getAllReservations(
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sort)
        );
    }

    // USER - own reservation
    // ADMIN - any reservation
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDto>
            getReservationById(
                @PathVariable Long id) {

        return ResponseEntity.ok(
                reservationService.getReservationById(id));
    }

    // ADMIN only
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto>
            updateReservation(
                @PathVariable Long id,
                @Valid @RequestBody
                ReservationRequestDto request) {

        return ResponseEntity.ok(
                reservationService.updateReservation(
                        id,
                        request));
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
