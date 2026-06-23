package com.example.userservice.advice;

import com.example.userservice.exception.UserAlreadyExistException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsExistingUserToBadRequest() {
        var response = handler.handleUserAlreadyExistException(new UserAlreadyExistException("exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "exists");
    }

    @Test
    void mapsUnexpectedExceptionToServerError() {
        var response = handler.handleGeneralException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "Something went wrong!");
    }
}
