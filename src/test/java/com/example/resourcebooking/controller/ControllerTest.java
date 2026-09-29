package com.example.resourcebooking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.resourcebooking.dto.LoginRequestDto;
import com.example.resourcebooking.dto.LoginResponseDto;
import com.example.resourcebooking.dto.ReservationRequestDto;
import com.example.resourcebooking.dto.ReservationResponseDto;
import com.example.resourcebooking.dto.UserRequestDto;
import com.example.resourcebooking.dto.UserResponseDto;
import com.example.resourcebooking.entity.ReservationStatus;
import com.example.resourcebooking.service.AuthService;
import com.example.resourcebooking.service.ReservationService;
import com.example.resourcebooking.service.UserService;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private ReservationController reservationController;

    @InjectMocks
    private UserController userController;

    @Test
    void login_shouldReturnOk() {

        LoginRequestDto request =
                Mockito.mock(LoginRequestDto.class);

        LoginResponseDto response =
                Mockito.mock(LoginResponseDto.class);

        when(authService.login(request))
                .thenReturn(response);

        ResponseEntity<LoginResponseDto> result =
                authController.login(request);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(authService).login(request);
    }

    @Test
    void createReservation_shouldReturnCreated() {

        ReservationRequestDto request =
                Mockito.mock(ReservationRequestDto.class);

        ReservationResponseDto response =
                Mockito.mock(ReservationResponseDto.class);

        when(reservationService.createReservation(request))
                .thenReturn(response);

        ResponseEntity<ReservationResponseDto> result =
                reservationController.createReservation(request);

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(reservationService)
                .createReservation(request);
    }

    @Test
    void getMyReservations_shouldReturnOk() {

        Page<ReservationResponseDto> page =
                new PageImpl<>(List.of());

        when(reservationService.getMyReservations(
                null,
                null,
                null,
                0,
                10,
                "id,asc"))
                .thenReturn(page);

        ResponseEntity<Page<ReservationResponseDto>> result =
                reservationController.getMyReservations(
                        null,
                        null,
                        null,
                        0,
                        10,
                        "id,asc");

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertNotNull(result.getBody());

        verify(reservationService)
                .getMyReservations(
                        null,
                        null,
                        null,
                        0,
                        10,
                        "id,asc");
    }

    @Test
    void getAllReservations_shouldReturnOk() {

        Page<ReservationResponseDto> page =
                new PageImpl<>(List.of());

        BigDecimal minPrice =
                new BigDecimal("100");

        BigDecimal maxPrice =
                new BigDecimal("1000");

        when(reservationService.getAllReservations(
                ReservationStatus.CONFIRMED,
                minPrice,
                maxPrice,
                1,
                20,
                "price,desc"))
                .thenReturn(page);

        ResponseEntity<Page<ReservationResponseDto>> result =
                reservationController.getAllReservations(
                        ReservationStatus.CONFIRMED,
                        minPrice,
                        maxPrice,
                        1,
                        20,
                        "price,desc");

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertNotNull(result.getBody());

        verify(reservationService)
                .getAllReservations(
                        ReservationStatus.CONFIRMED,
                        minPrice,
                        maxPrice,
                        1,
                        20,
                        "price,desc");
    }

    @Test
    void getReservationById_shouldReturnOk() {

        ReservationResponseDto response =
                Mockito.mock(ReservationResponseDto.class);

        when(reservationService.getReservationById(1L))
                .thenReturn(response);

        ResponseEntity<ReservationResponseDto> result =
                reservationController.getReservationById(1L);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(reservationService)
                .getReservationById(1L);
    }

    @Test
    void updateReservation_shouldReturnOk() {

        ReservationRequestDto request =
                Mockito.mock(ReservationRequestDto.class);

        ReservationResponseDto response =
                Mockito.mock(ReservationResponseDto.class);

        when(reservationService.updateReservation(
                1L,
                request))
                .thenReturn(response);

        ResponseEntity<ReservationResponseDto> result =
                reservationController.updateReservation(
                        1L,
                        request);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(reservationService)
                .updateReservation(
                        1L,
                        request);
    }

    @Test
    void deleteReservation_shouldReturnNoContent() {

        ResponseEntity<Void> result =
                reservationController.deleteReservation(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                result.getStatusCode());

        verify(reservationService)
                .deleteReservation(1L);
    }

    @Test
    void createUser_shouldReturnCreated() {

        UserRequestDto request =
                Mockito.mock(UserRequestDto.class);

        UserResponseDto response =
                Mockito.mock(UserResponseDto.class);

        when(userService.createUser(request))
                .thenReturn(response);

        ResponseEntity<UserResponseDto> result =
                userController.createUser(request);

        assertEquals(
                HttpStatus.CREATED,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(userService)
                .createUser(request);
    }

    @Test
    void getAllUsers_shouldReturnOk() {

        List<UserResponseDto> users =
                List.of();

        when(userService.getAllUsers())
                .thenReturn(users);

        ResponseEntity<List<UserResponseDto>> result =
                userController.getAllUsers();

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertNotNull(result.getBody());

        verify(userService)
                .getAllUsers();
    }

    @Test
    void getUserById_shouldReturnOk() {

        UserResponseDto response =
                Mockito.mock(UserResponseDto.class);

        when(userService.getUserById(1L))
                .thenReturn(response);

        ResponseEntity<UserResponseDto> result =
                userController.getUserById(1L);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(userService)
                .getUserById(1L);
    }

    @Test
    void updateUser_shouldReturnOk() {

        UserRequestDto request =
                Mockito.mock(UserRequestDto.class);

        UserResponseDto response =
                Mockito.mock(UserResponseDto.class);

        when(userService.updateUser(
                1L,
                request))
                .thenReturn(response);

        ResponseEntity<UserResponseDto> result =
                userController.updateUser(
                        1L,
                        request);

        assertEquals(
                HttpStatus.OK,
                result.getStatusCode());

        assertEquals(
                response,
                result.getBody());

        verify(userService)
                .updateUser(
                        1L,
                        request);
    }

    @Test
    void deleteUser_shouldReturnNoContent() {

        ResponseEntity<Void> result =
                userController.deleteUser(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                result.getStatusCode());

        verify(userService)
                .deleteUser(1L);
    }
}