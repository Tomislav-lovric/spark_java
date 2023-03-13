package com.example.spark_project.user;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping(path = "/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    //standard controller with @Valid annotation for our validations

    private final UserService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody @Valid PasswordResetRequest request
    ) throws MessagingException, UnsupportedEncodingException {
        service.forgotPassword(request);
        return ResponseEntity.ok("Email has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody @Valid NewPasswordRequest request,
            @RequestParam String resetToken
    ) {
        service.resetPassword(request, resetToken);
        return ResponseEntity.ok("Password has been reset");
    }
}
