package com.example.resourcebooking.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.Role;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Value("${seed.admin.username}")
    private String adminUsername;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.user.username}")
    private String userUsername;

    @Value("${seed.user.password}")
    private String userPassword;

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByUsername(adminUsername)) {

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(
                        passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername(userUsername)) {

                User user = new User();
                user.setUsername(userUsername);
                user.setPassword(
                        passwordEncoder.encode(userPassword));
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