package com.example.resourcebooking.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {

        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        return toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(
            Long id,
            UserRequestDto request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        String username = request.getUsername().trim();

        if (!user.getUsername().equals(username)
                && userRepository.existsByUsername(username)) {

            throw new IllegalArgumentException(
                    "Username already exists");
        }

        user.setUsername(username);
        user.setRole(request.getRole());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()));
        }

        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
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
