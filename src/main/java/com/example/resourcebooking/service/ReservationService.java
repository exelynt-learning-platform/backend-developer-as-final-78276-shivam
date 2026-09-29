package com.example.resourcebooking.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;

import com.example.resourcebooking.dto.ReservationRequestDto;
import com.example.resourcebooking.dto.ReservationResponseDto;
import com.example.resourcebooking.entity.ReservationStatus;

public interface ReservationService {

    ReservationResponseDto createReservation(
            ReservationRequestDto request);

    ReservationResponseDto getReservationById(
            Long id);

    Page<ReservationResponseDto> getMyReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort);

    Page<ReservationResponseDto> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort);

    ReservationResponseDto updateReservation(
            Long id,
            ReservationRequestDto request);

    void deleteReservation(Long id);
}
