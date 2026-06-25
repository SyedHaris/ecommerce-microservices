package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.LoginResponseDTO;
import com.example.userservice.dto.SignupRequestDTO;
import com.example.userservice.dto.SignupResponseDTO;
import com.example.userservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponseDTO signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        return authenticationService.signup(signupRequestDTO);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return authenticationService.login(loginRequestDTO);
    }
}
