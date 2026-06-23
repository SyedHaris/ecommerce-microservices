package com.example.userservice.service.impl;

import com.example.userservice.constans.UserRole;
import com.example.userservice.dto.LoginRequestDTO;
import com.example.userservice.dto.SignupRequestDTO;
import com.example.userservice.entity.User;
import com.example.userservice.exception.UserAlreadyExistException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "jwtTtl", 60000);
    }

    @Test
    void signupPersistsNewUserWithEncodedPassword() {
        SignupRequestDTO request = new SignupRequestDTO("haris@example.com", "secret1", "haris");
        when(userRepository.findByEmail("haris@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        var response = authenticationService.signup(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("haris@example.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    void signupRejectsExistingEmail() {
        when(userRepository.findByEmail("haris@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.signup(new SignupRequestDTO("haris@example.com", "secret1", "haris")))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessage("User already exist");
    }

    @Test
    void loginReturnsJwtMetadata() {
        LoginRequestDTO request = new LoginRequestDTO("haris@example.com", "secret1");
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("haris@example.com")
                .password("encoded")
                .roles("USER")
                .build();
        User user = new User(7L, "haris", "haris@example.com", "encoded", UserRole.USER);
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + 60000);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("haris@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generate(userDetails, user, 60000)).thenReturn("token");
        when(jwtUtil.extractCreatedAt("token")).thenReturn(issuedAt);
        when(jwtUtil.extractExpirationDate("token")).thenReturn(expiresAt);

        var response = authenticationService.login(request);

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.createdAt()).isEqualTo(issuedAt);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void loginRejectsUnauthenticatedPrincipal() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new LoginRequestDTO("haris@example.com", "secret1")))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Failed to authenticate");
    }

    @Test
    void loginRejectsMissingUserAfterAuthentication() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("missing@example.com")
                .password("encoded")
                .roles("USER")
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(new LoginRequestDTO("missing@example.com", "secret1")))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
