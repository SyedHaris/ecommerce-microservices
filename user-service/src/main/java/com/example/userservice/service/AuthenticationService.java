package com.example.userservice.service;

import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.LoginResponseDTO;
import com.example.userservice.dto.SignupRequestDTO;
import com.example.userservice.dto.SignupResponseDTO;

public interface AuthenticationService {

    SignupResponseDTO signup(SignupRequestDTO signupRequestDTO);

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

}
