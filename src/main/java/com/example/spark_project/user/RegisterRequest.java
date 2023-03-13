package com.example.spark_project.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name should not be blank")
    private String firstName;

    @NotBlank(message = "Last name should not be blank")
    private String lastName;

    @Email(message = "Please enter the valid email")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,16}$",
            message = "Password should contain at least one lower case letter, one upper case letter," +
                    " one number, one special character, and it should contain 8 to 16 character")
    private String password;

    //We might not need validation here because we will be checking if password and repeatPassword
    //are equal in userService anyway
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,16}$",
            message = "Repeated password should contain at least one lower case letter, one upper case letter," +
                    " one number, one special character, and it should contain 8 to 16 character")
    private String repeatPassword;
}
