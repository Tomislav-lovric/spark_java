package com.example.spark_project.image;

import com.example.spark_project.user.Role;
import com.example.spark_project.user.User;
import com.example.spark_project.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    private Image image;
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

        image = Image.builder()
                .filename("Earth.gif")
                .mimeType("image/gif")
                .data(new byte[] {0x00, 0x01, 0x02, 0x03})
                .size(1024L)
                .createdAt(LocalDateTime.now().withNano(0))
                .user(user)
                .build();
        imageRepository.save(image);

        Image image2 = Image.builder()
                .filename("Earth1.gif")
                .mimeType("image/gif")
                .data(new byte[] {0x00, 0x01, 0x02, 0x03})
                .size(1024L)
                .createdAt(LocalDateTime.now().withNano(0))
                .user(user)
                .build();
        imageRepository.save(image2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testExistsByFilenameAndUserShouldReturnTrue() {
        // when
        boolean exists = imageRepository.existsByFilenameAndUser(image.getFilename(), user);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByFilenameAndUserShouldReturnFalse() {
        // given
        String imageName = "toFail";

        // when
        boolean exists = imageRepository.existsByFilenameAndUser(imageName, user);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByFilenameAndUserShouldReturnImage() {
        // when
        Image expected = imageRepository.findByFilenameAndUser(image.getFilename(), user).get();

        // then
        assertThat(expected).isNotNull();
    }

    @Test
    void deleteByFilenameAndUser() {
        // given
        imageRepository.deleteByFilenameAndUser(image.getFilename(), user);

        // when
        Optional<Image> deletedImage = imageRepository.findById(image.getId());

        // then
        assertThat(deletedImage).isEmpty();
    }

    @Test
    void testFindByCreatedAtAndUserShouldReturnImageList() {
        // given
        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<Image> imageList = imageRepository.findByCreatedAtAndUser(image.getCreatedAt(), user, pageable);

        // then
        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isEqualTo(2);
    }

    @Test
    void testExistsByCreatedAtAndUserShouldReturnTrue() {
        // when
        boolean exists = imageRepository.existsByCreatedAtAndUser(image.getCreatedAt(), user);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByCreatedAtAndUserShouldReturnFalse() {
        // given
        LocalDateTime dateTime = LocalDateTime.now().plusSeconds(1);

        // when
        boolean exists = imageRepository.existsByCreatedAtAndUser(dateTime, user);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByUserOrderBySizeAscShouldReturnImageList() {
        // when
        List<Image> imageList = imageRepository.findByUserOrderBySizeAsc(user);

        // then
        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isEqualTo(2);
    }

    @Test
    void testFindByUserOrderBySizeDescShouldReturnImageList() {
        // when
        List<Image> imageList = imageRepository.findByUserOrderBySizeDesc(user);

        // then
        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isEqualTo(2);
    }

    @Test
    void testExistsByUserShouldReturnTrue() {
        // when
        boolean exists = imageRepository.existsByUser(user);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByUserShouldReturnFalse() {
        // given
        String resetToken2 = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);
        User user2 = User.builder()
                .firstName("John2")
                .lastName("Evans2")
                .email("john2_evans2@gmail.com")
                .password("Test.123")
                .repeatPassword("Test.123")
                .role(Role.USER)
                .resetPasswordToken(resetToken2)
                .build();

        // when
        boolean exists = imageRepository.existsByUser(user2);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByCreatedAtAndUserOrderBySizeAscShouldReturnImageList() {
        // given
        LocalDateTime createdAt = image.getCreatedAt();
        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<Image> imageList = imageRepository.findByCreatedAtAndUserOrderBySizeAsc(createdAt, user, pageable);

        // then
        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isEqualTo(2);
    }

    @Test
    void testFindByCreatedAtAndUserOrderBySizeDescShouldReturnImageList() {
        // given
        LocalDateTime createdAt = image.getCreatedAt();
        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<Image> imageList = imageRepository.findByCreatedAtAndUserOrderBySizeDesc(createdAt, user, pageable);

        // then
        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isEqualTo(2);
    }
}