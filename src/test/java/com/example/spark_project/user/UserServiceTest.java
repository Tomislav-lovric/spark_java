package com.example.spark_project.user;

import com.example.spark_project.exception.InvalidPasswordResetTokenException;
import com.example.spark_project.exception.InvalidRepeatedPasswordException;
import com.example.spark_project.exception.UserAlreadyExistsException;
import com.example.spark_project.security.JwtService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
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

    @Mock
    private JavaMailSender mailSender;

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
        // given
        String token = "token";

        // when
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn(token);

        AuthenticationResponse response = userService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    void testRegisterShouldThrowUserAlreadyExistsException() {
        // when
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with that email already exists");
    }

    @Test
    void testRegisterShouldThrowInvalidRepeatedPasswordException() {
        // given
        String missMatchPassword = "Pass.123";
        registerRequest.setRepeatPassword(missMatchPassword);

        // when
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);

        // then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(InvalidRepeatedPasswordException.class)
                .hasMessageContaining("Password do not match");
    }

    @Test
    void testLoginShouldReturnAuthenticationResponse() {
        // given
        String token = "token";
        Authentication authentication = mock(Authentication.class);

        // when
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword()))
        ).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn(token);

        AuthenticationResponse response = userService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );
    }

    @Test
    void testLoginShouldThrowUsernameNotFoundException() {
        // when
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);

        // then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with " + loginRequest.getEmail() + " not found");
    }

    @Test
    void testForgotPasswordShouldReturnVoid() throws MessagingException, UnsupportedEncodingException {
        // given
        PasswordResetRequest request = PasswordResetRequest.builder().email("john_evans@gmail.com").build();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        // when
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        userService.forgotPassword(request);

        // then
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testForgotPasswordShouldThrowUsernameNotFoundException() {
        // given
        PasswordResetRequest request = PasswordResetRequest.builder().email("john_evans@gmail.com").build();

        // when
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // then
        assertThatThrownBy(() -> userService.forgotPassword(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with that email does not exist");
    }

    @Test
    void testResetPasswordShouldReturnVoid() {
        // given
        NewPasswordRequest request = NewPasswordRequest.builder()
                .password("NewTest.123")
                .repeatPassword("NewTest.123")
                .build();
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);

        // when
        when(userRepository.existsByResetPasswordToken(resetToken)).thenReturn(true);
        when(userRepository.findByResetPasswordToken(resetToken)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        userService.resetPassword(request, resetToken);

        // then
        verify(userRepository, times(1)).existsByResetPasswordToken(resetToken);
        verify(userRepository, times(1)).findByResetPasswordToken(resetToken);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetPasswordShouldThrowInvalidPasswordResetTokenException() {
        // given
        NewPasswordRequest request = NewPasswordRequest.builder()
                .password("NewTest.123")
                .repeatPassword("NewTest.123")
                .build();
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);

        // when
        when(userRepository.existsByResetPasswordToken(resetToken)).thenReturn(false);

        // then
        assertThatThrownBy(() -> userService.resetPassword(request, resetToken))
                .isInstanceOf(InvalidPasswordResetTokenException.class)
                .hasMessageContaining("Invalid password reset token");
    }

    @Test
    void testResetPasswordShouldThrowInvalidRepeatedPasswordException() {
        // given
        NewPasswordRequest request = NewPasswordRequest.builder()
                .password("NewTest.123")
                .repeatPassword("MissMatch.123")
                .build();
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);

        // when
        when(userRepository.existsByResetPasswordToken(resetToken)).thenReturn(true);

        // then
        assertThatThrownBy(() -> userService.resetPassword(request, resetToken))
                .isInstanceOf(InvalidRepeatedPasswordException.class)
                .hasMessageContaining("Passwords do not match");
    }
}