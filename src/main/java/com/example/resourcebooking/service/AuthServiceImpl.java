package com.example.resourcebooking.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.resourcebooking.dto.LoginRequestDto;
import com.example.resourcebooking.dto.LoginResponseDto;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByUsername(
                request.getUsername())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid username or password");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name());

        return new LoginResponseDto(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole().name());
    }
}
