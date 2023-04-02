package com.example.spark_project.user;

import com.example.spark_project.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);
        user = User.builder()
                .firstName("John")
                .lastName("Evans")
                .email("john_evans@gmail.com")
                .password(passwordEncoder.encode("Test.123"))
                .repeatPassword(passwordEncoder.encode("Test.123"))
                .role(Role.USER)
                .resetPasswordToken(resetToken)
                .build();

        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Evans")
                .email("john_evans@gmail.com")
                .password("Test.123")
                .repeatPassword("Test.123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john_evans@gmail.com")
                .password("Test.123")
                .build();
    }

    @Test
    void testRegisterShouldReturnAuthenticationResponse() {
        // when
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthenticationResponse response = userService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    void testLoginShouldReturnAuthenticationResponse() {
        // when
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthenticationResponse response = userService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    void testForgotPasswordShouldReturnVoid() {
        // when

        // then

    }

    @Test
    void testResetPasswordShouldReturnVoid() {
        // when

        // then

    }
}