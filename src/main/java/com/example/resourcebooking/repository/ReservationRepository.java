package com.example.resourcebooking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.entity.Resource;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
                JpaSpecificationExecutor<Reservation> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

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
