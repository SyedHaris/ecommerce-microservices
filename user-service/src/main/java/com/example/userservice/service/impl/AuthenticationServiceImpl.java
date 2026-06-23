package com.example.userservice.service.impl;

import com.example.userservice.constans.UserRole;
import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.LoginResponseDTO;
import com.example.userservice.dto.SignupRequestDTO;
import com.example.userservice.dto.SignupResponseDTO;
import com.example.userservice.entity.User;
import com.example.userservice.exception.UserAlreadyExistException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthenticationService;
import com.example.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    @Value("${security.jwt.expiration-time}")
    private Integer jwtTtl;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO signupRequestDTO) {
        if (userRepository.findByEmail(signupRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistException("User already exist");
        }

        User user = saveUser(signupRequestDTO);

        return new SignupResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDTO.email(), loginRequestDTO.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Failed to authenticate");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generate(userDetails, user, jwtTtl);

        return new LoginResponseDTO(
                token,
                jwtUtil.extractCreatedAt(token),
                jwtUtil.extractExpirationDate(token)
        );
    }

    private User saveUser(SignupRequestDTO signupRequestDTO) {
        User user = new User();
        user.setEmail(signupRequestDTO.email());
        user.setPassword(passwordEncoder.encode(signupRequestDTO.password()));
        user.setUsername(signupRequestDTO.username());
        user.setRole(UserRole.USER);
        userRepository.save(user);
        return user;
    }

}
