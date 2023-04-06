package com.example.spark_project.user;

import com.example.spark_project.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    //needs either JwtAuthenticationFilter mock or JwtService mock, but not both
//    @MockBean
//    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    private final String jwtToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private static final String END_POINT_PATH = "/api/v1/user";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegister() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Evans")
                .email("john_evans@gmail.com")
                .password("Test.123")
                .repeatPassword("Test.123")
                .build();

        AuthenticationResponse response = new AuthenticationResponse(jwtToken);

        // when
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        // then
        mockMvc.perform(post(END_POINT_PATH + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(jwtToken));
    }

    @Test
    void login() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john_evans@gmail.com")
                .password("Test.123")
                .build();

        AuthenticationResponse response = new AuthenticationResponse(jwtToken);

        // when
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        // then
        mockMvc.perform(post(END_POINT_PATH + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken));
    }

    @Test
    void forgotPassword() throws Exception {
        // given
        PasswordResetRequest request = new PasswordResetRequest("john_evans@gmail.com");
        String response = "Email has been sent";

        // when
        doNothing().when(userService).forgotPassword(request);

        // then
        mockMvc.perform(post(END_POINT_PATH + "/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(response));
    }

    @Test
    void resetPassword() throws Exception {
        // given
        NewPasswordRequest request = NewPasswordRequest.builder()
                .password("NewTest.123")
                .repeatPassword("NewTest.123")
                .build();
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);
        String response = "Password has been reset";

        // when
        doNothing().when(userService).resetPassword(request, resetToken);

        // then
        mockMvc.perform(post(END_POINT_PATH + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("resetToken", resetToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(response));
    }
}