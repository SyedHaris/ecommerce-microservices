package com.example.userservice.controller;

import com.example.userservice.constans.UserRole;
import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.LoginResponseDTO;
import com.example.userservice.dto.SignupRequestDTO;
import com.example.userservice.dto.SignupResponseDTO;
import com.example.userservice.service.AuthenticationService;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationControllerTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final AuthenticationController controller = new AuthenticationController(authenticationService);

    @Test
    void signupDelegatesToService() {
        SignupRequestDTO request = new SignupRequestDTO("haris@example.com", "secret1", "haris");
        SignupResponseDTO expected = new SignupResponseDTO(1L, "haris@example.com", "haris", UserRole.USER);
        when(authenticationService.signup(request)).thenReturn(expected);

        assertThat(controller.signup(request)).isEqualTo(expected);
    }

    @Test
    void loginDelegatesToService() {
        LoginRequestDTO request = new LoginRequestDTO("haris@example.com", "secret1");
        LoginResponseDTO expected = new LoginResponseDTO("token", new Date(), new Date());
        when(authenticationService.login(request)).thenReturn(expected);

        assertThat(controller.login(request)).isEqualTo(expected);
    }
}
