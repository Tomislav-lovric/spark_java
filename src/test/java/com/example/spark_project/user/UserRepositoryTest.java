package com.example.spark_project.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);
        user = User.builder()
                .firstName("John")
                .lastName("Evans")
                .email("john_evans@gmail.com")
                .password("Test.123")
                .repeatPassword("Test.123")
                .role(Role.USER)
                .resetPasswordToken(resetToken)
                .build();
        userRepository.save(user);
    }

    @Test
    void testFindByEmailShouldReturnOptionalUser() {
        // when
        User expected = userRepository.findByEmail(user.getEmail()).get();

        // then
        assertThat(expected).isNotNull();
    }

    @Test
    void testExistsByEmailShouldReturnTrue() {
        // when
        boolean existsByEmail = userRepository.existsByEmail(user.getEmail());

        // then
        assertThat(existsByEmail).isTrue();
    }

    @Test
    void testExistsByEmailShouldReturnFalse() {
        // given
        String emailToFail = "test@email.com";

        // when
        boolean existsByEmail = userRepository.existsByEmail(emailToFail);

        // then
        assertThat(existsByEmail).isFalse();
    }

    @Test
    void testExistsByResetPasswordTokenShouldReturnTrue() {
        // when
        boolean existsByResetPwToken = userRepository.existsByResetPasswordToken(user.getResetPasswordToken());

        // then
        assertThat(existsByResetPwToken).isTrue();
    }

    @Test
    void testExistsByResetPasswordTokenShouldReturnFalse() {
        // given
        String resetTokenToFail = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);

        // when
        boolean existsByResetPwToken = userRepository.existsByResetPasswordToken(resetTokenToFail);

        // then
        assertThat(existsByResetPwToken).isFalse();
    }

    @Test
    void findByResetPasswordToken() {
        // when
        User expected = userRepository.findByResetPasswordToken(user.getResetPasswordToken());

        // then
        assertThat(expected).isNotNull();
    }

    @Test
    void findUserByEmail() {
        // when
        User expected = userRepository.findUserByEmail(user.getEmail());

        // then
        assertThat(expected).isNotNull();
    }
}