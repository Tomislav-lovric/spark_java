package com.example.spark_project.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Email(message = "Please enter valid email")
    private String email;

    //We probably don't even need pattern in login, only in register request
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,16}$",
            message = "Password should contain at least one lower case letter, one upper case letter," +
                    " one number, one special character, and it should contain 8 to 16 character")
    private String password;
}
