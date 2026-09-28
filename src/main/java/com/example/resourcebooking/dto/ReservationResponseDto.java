package com.example.resourcebooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.resourcebooking.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationResponseDto {
    private Long id;
    private Long userId;
    private Long resourceId;
    private String userName;
    private String resourceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private ReservationStatus status;
    private String resourceDescription;
}
