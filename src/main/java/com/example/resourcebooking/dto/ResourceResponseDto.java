package com.example.resourcebooking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
}
