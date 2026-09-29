package com.example.resourcebooking.dto;

import com.example.resourcebooking.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private Role role;
}
