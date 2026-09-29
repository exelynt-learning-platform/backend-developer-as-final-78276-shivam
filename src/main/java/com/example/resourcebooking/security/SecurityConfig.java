package com.example.resourcebooking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))

            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/auth/login").permitAll()

                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**")
                .permitAll()

                .requestMatchers("/api/users/**")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/resources/**")
                .authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/resources/**")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/resources/**")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/resources/**")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/reservations")
                .authenticated()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reservations/my")
                .authenticated()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reservations")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reservations/*")
                .authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/reservations/*")
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/reservations/*")
                .hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
