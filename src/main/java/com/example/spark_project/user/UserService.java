package com.example.spark_project.user;

import com.example.spark_project.exception.InvalidPasswordResetTokenException;
import com.example.spark_project.exception.InvalidRepeatedPasswordException;
import com.example.spark_project.exception.UserAlreadyExistsException;
import com.example.spark_project.security.JwtService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    public AuthenticationResponse register(RegisterRequest request) {
        //check if user with that email already exists
        if (repository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with that email already exists");
        }
        //check if passwords match
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new InvalidRepeatedPasswordException("Password do not match");
        }
        //if both of those are ok build user out of the request
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .repeatPassword(passwordEncoder.encode(request.getRepeatPassword()))
                .role(Role.USER)
                .build();
        //and save it to our db
        repository.save(user);
        //then build token out of that user
        var jwtToken = jwtService.generateToken(user);
        //and send it back to him(user)
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        //check if user with that email already exists
        if (!repository.existsByEmail(request.getEmail())) {
            throw new UsernameNotFoundException("User with " + request.getEmail() + " not found");
        }
        //if he does authenticate him
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void forgotPassword(PasswordResetRequest request) throws MessagingException, UnsupportedEncodingException {
        if (!repository.existsByEmail(request.getEmail())) {
            throw new UsernameNotFoundException("User with that email does not exist");
        }

        var optionalUser = repository.findByEmail(request.getEmail());
        var user = optionalUser.get();

        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);

        user.setResetPasswordToken(resetToken);
        repository.save(user);

        sendEmail(request.getEmail(), resetToken);
    }

    private void sendEmail(String userEmail, String resetToken) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("project.passreset@gmail.com", "Password Reset");
        helper.setTo(userEmail);

        String subject = "Password Reset Link";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Use (in Postman) the link below to change your password:</p>"
                + "<p>http://localhost:8080/api/v1/user/reset-password?resetToken=" + resetToken +"</p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    public void resetPassword(NewPasswordRequest request, String resetToken) {
        if (!repository.existsByResetPasswordToken(resetToken)) {
            throw new InvalidPasswordResetTokenException("Invalid password reset token");
        }

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new InvalidRepeatedPasswordException("Passwords do not match");
        }

        var user = repository.findByResetPasswordToken(resetToken);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRepeatPassword(passwordEncoder.encode(request.getRepeatPassword()));
        user.setResetPasswordToken(null);

        repository.save(user);
    }

}
