package com.example.resourcebooking.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.resourcebooking.dto.UserRequestDto;
import com.example.resourcebooking.dto.UserResponseDto;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.exception.UserNotFoundException;
import com.example.resourcebooking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto createUser(UserRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return toDto(userRepository.save(user));
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        return toDto(user);
    }

    @Override
    public UserResponseDto updateUser(
            Long id,
            UserRequestDto request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(
                        request.getUsername())) {

            throw new IllegalArgumentException(
                    "Username already exists");
        }

        user.setUsername(request.getUsername());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()));
        }

        user.setRole(request.getRole());

        return toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        userRepository.delete(user);
    }

    private UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole());
    }
}
