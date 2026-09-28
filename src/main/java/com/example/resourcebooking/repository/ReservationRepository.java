package com.example.resourcebooking.repository;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.entity.Resource;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
                JpaSpecificationExecutor<Reservation> {

    boolean existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Resource resource,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime);

    boolean existsByResourceAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Resource resource,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime,
            Long id);
}
