package com.example.resourcebooking.service;

import java.util.List;

import com.example.resourcebooking.dto.UserRequestDto;
import com.example.resourcebooking.dto.UserResponseDto;

public interface UserService {

    UserResponseDto createUser(UserRequestDto request);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);

    UserResponseDto updateUser(Long id, UserRequestDto request);

    void deleteUser(Long id);
}
