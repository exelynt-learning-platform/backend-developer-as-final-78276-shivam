package com.example.resourcebooking.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.resourcebooking.entity.*;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(
                        passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(
                        passwordEncoder.encode("User@123"));
                user.setRole(Role.USER);

                userRepository.save(user);
            }

            if (resourceRepository.count() == 0) {

                Resource conferenceRoom = new Resource(
                        null,
                        "Conference Room",
                        "Meeting and conference room",
                        new BigDecimal("1500.00"),
                        true);

                Resource laptop = new Resource(
                        null,
                        "Laptop",
                        "Dell business laptop",
                        new BigDecimal("800.00"),
                        true);

                Resource projector = new Resource(
                        null,
                        "Projector",
                        "HD projector for presentations",
                        new BigDecimal("500.00"),
                        true);

                resourceRepository.save(conferenceRoom);
                resourceRepository.save(laptop);
                resourceRepository.save(projector);
            }
        };
    }
}
