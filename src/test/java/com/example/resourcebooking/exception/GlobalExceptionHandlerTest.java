package com.example.resourcebooking.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleUnauthorized_shouldReturn401() {

        UnauthorizedException ex =
                mock(UnauthorizedException.class);

        when(ex.getMessage())
                .thenReturn("Authentication is required");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnauthorized(ex);

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required");
    }

    @Test
    void handleUserNotFound_shouldReturn404() {

        UserNotFoundException ex =
                mock(UserNotFoundException.class);

        when(ex.getMessage())
                .thenReturn("User not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUserNotFound(ex);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "User Not Found",
                "User not found");
    }

    @Test
    void handleResourceNotFound_shouldReturn404() {

        ResourceNotFoundException ex =
                mock(ResourceNotFoundException.class);

        when(ex.getMessage())
                .thenReturn("Resource not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleResourceNotFound(ex);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                "Resource not found");
    }

    @Test
    void handleReservationNotFound_shouldReturn404() {

        ReservationNotFoundException ex =
                mock(ReservationNotFoundException.class);

        when(ex.getMessage())
                .thenReturn("Reservation not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleReservationNotFound(ex);

        assertResponse(
                response,
                HttpStatus.NOT_FOUND,
                "Reservation Not Found",
                "Reservation not found");
    }

    @Test
    void handleForbidden_shouldReturn403() {

        ForbiddenException ex =
                mock(ForbiddenException.class);

        when(ex.getMessage())
                .thenReturn("Access denied");

        ResponseEntity<Map<String, Object>> response =
                handler.handleForbidden(ex);

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied");
    }

    @Test
    void handleConflict_shouldReturn409() {

        ConflictException ex =
                mock(ConflictException.class);

        when(ex.getMessage())
                .thenReturn("Resource already booked");

        ResponseEntity<Map<String, Object>> response =
                handler.handleConflict(ex);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Conflict",
                "Resource already booked");
    }

    @Test
    void handleBadRequest_shouldReturn400() {

        IllegalArgumentException ex =
                mock(IllegalArgumentException.class);

        when(ex.getMessage())
                .thenReturn("Invalid request");

        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(ex);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid request");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldErrors() {

        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                mock(FieldError.class);

        when(fieldError.getField())
                .thenReturn("username");

        when(fieldError.getDefaultMessage())
                .thenReturn("Username is required");

        when(bindingResult.getFieldErrors())
                .thenReturn(java.util.List.of(fieldError));

        when(ex.getBindingResult())
                .thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().get("status"));

        assertEquals(
                "Validation Failed",
                response.getBody().get("error"));

        @SuppressWarnings("unchecked")
        Map<String, String> messages =
                (Map<String, String>)
                        response.getBody().get("messages");

        assertEquals(
                "Username is required",
                messages.get("username"));

        assertNotNull(
                response.getBody().get("timestamp"));
    }

    @Test
    void handleTypeMismatch_shouldReturn400() {

        MethodArgumentTypeMismatchException ex =
                mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName())
                .thenReturn("id");

        ResponseEntity<Map<String, Object>> response =
                handler.handleTypeMismatch(ex);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid value for parameter: id");
    }

    @Test
    void handleUnreadableBody_shouldReturn400() {

        HttpMessageNotReadableException ex =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnreadableBody(ex);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Request body is invalid or malformed");
    }

    @Test
    void handleMissingParameter_shouldReturn400() {

        MissingServletRequestParameterException ex =
                mock(MissingServletRequestParameterException.class);

        when(ex.getParameterName())
                .thenReturn("resourceId");

        ResponseEntity<Map<String, Object>> response =
                handler.handleMissingParameter(ex);

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Required parameter is missing: resourceId");
    }

    @Test
    void handleMediaType_shouldReturn415() {

        HttpMediaTypeNotSupportedException ex =
                mock(HttpMediaTypeNotSupportedException.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleMediaType(ex);

        assertResponse(
                response,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported Media Type",
                "Unsupported request content type");
    }

    @Test
    void handleDataIntegrity_shouldReturn409() {

        DataIntegrityViolationException ex =
                mock(DataIntegrityViolationException.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrity(ex);

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Conflict",
                "Request conflicts with existing database data");
    }

    @Test
    void handleUnexpectedException_shouldReturn500() {

        Exception ex =
                mock(Exception.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnexpectedException(ex);

        assertResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred");
    }

    private void assertResponse(
            ResponseEntity<Map<String, Object>> response,
            HttpStatus expectedStatus,
            String expectedError,
            String expectedMessage) {

        assertEquals(
                expectedStatus,
                response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(
                expectedStatus.value(),
                response.getBody().get("status"));

        assertEquals(
                expectedError,
                response.getBody().get("error"));

        assertEquals(
                expectedMessage,
                response.getBody().get("message"));

        assertNotNull(
                response.getBody().get("timestamp"));
    }
}