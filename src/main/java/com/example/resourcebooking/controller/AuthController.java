package com.example.resourcebooking.controller;

import com.example.resourcebooking.dto.LoginRequestDto;
import com.example.resourcebooking.dto.LoginResponseDto;
import com.example.resourcebooking.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        return ResponseEntity.ok(
                authService.login(request));
    }
}
