package com.example.resourcebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String tokenType;
    private String username;
    private String role;
}
