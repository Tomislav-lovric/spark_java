package com.example.spark_project.image;

import com.example.spark_project.exception.FileNotAnImageException;
import com.example.spark_project.exception.ImageAlreadyExistsException;
import com.example.spark_project.exception.ImageNotFoundException;
import com.example.spark_project.security.JwtService;
import com.example.spark_project.user.Role;
import com.example.spark_project.user.User;
import com.example.spark_project.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final String TOKEN = "bearer token";

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ImageService imageService;

    private Image image;
    private User user;
    private MockMultipartFile file;

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

        image = Image.builder()
                .filename("Earth.gif")
                .mimeType("image/gif")
                .data(new byte[] {0x00, 0x01, 0x02, 0x03})
                .size(4L)
                .createdAt(LocalDateTime.now().withSecond(0).withNano(0))
                .user(user)
                .build();

        file = new MockMultipartFile(
                "Earth",
                "Earth.gif",
                "image/gif",
                image.getData()
        );
    }

    @Test
    void testGetImageShouldReturnImage() {
        // given
        String filename = "Earth.gif";

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.findByFilenameAndUser(filename, user)).thenReturn(Optional.of(image));

        Image expected = imageService.getImage(filename, TOKEN);

        // then
        assertThat(expected).isNotNull();
        assertThat(expected.getFilename()).isEqualTo(image.getFilename());
        assertThat(expected.getMimeType()).isEqualTo(image.getMimeType());
        assertThat(expected.getData()).isEqualTo(image.getData());
        assertThat(expected.getSize()).isEqualTo(image.getSize());
        assertThat(expected.getCreatedAt()).isEqualTo(image.getCreatedAt());
        assertThat(expected.getUser()).isEqualTo(image.getUser());
    }

    @Test
    void testGetImageShouldThrowImageNotFoundException() {
        // given
        String filename = "Earth.gif";

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.findByFilenameAndUser(filename, user)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> imageService.getImage(filename, TOKEN))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining("Image " + filename + " does not exist");
    }

    @Test
    void testUploadImageShouldReturnImageResponse() throws IOException {
        // given
        // These two lines are necessary for the createImageLink method
        // Without them, we get the exception, specifically:
        // java.lang.IllegalStateException: No current ServletRequestAttributes
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        String link = "http://localhost/api/v1/image/" + file.getOriginalFilename();

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)).thenReturn(false);
        when(imageRepository.save(any(Image.class))).thenReturn(image);

        ImageResponse expected = imageService.uploadImage(file, TOKEN);

        // then
        assertThat(expected).isNotNull();
        assertThat(expected.getImageLink()).isEqualTo(link);
    }

    @Test
    void testUploadImageShouldThrowFileNotAnImageException() {
        // given
        MockMultipartFile fileToFail = new MockMultipartFile(
                "Earth",
                "Earth.fail",
                "fail",
                image.getData()
        );

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        // then
        assertThatThrownBy(() -> imageService.uploadImage(fileToFail, TOKEN))
                .isInstanceOf(FileNotAnImageException.class)
                .hasMessageContaining("File you are trying to upload is not an image");
    }

    @Test
    void testUploadImageShouldThrowImageAlreadyExistsException() {
        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)).thenReturn(true);

        // then
        assertThatThrownBy(() -> imageService.uploadImage(file, TOKEN))
                .isInstanceOf(ImageAlreadyExistsException.class)
                .hasMessageContaining("Image with that filename already exists");
    }

    @Test
    void testChangeImageShouldReturnImageResponse() {
        // given

        // when

        // then
    }

    @Test
    void testChangeImageShouldThrow() {
        // given

        // when

        // then
    }

    @Test
    void testDeleteImageShouldReturnString() {
        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.existsByFilenameAndUser(image.getFilename(), user)).thenReturn(true);
        doNothing().when(imageRepository).deleteByFilenameAndUser(image.getFilename(), user);

        String expected = imageService.deleteImage(image.getFilename(), TOKEN);

        // then
        assertThat(expected).isNotNull();
        verify(imageRepository, times(1)).deleteByFilenameAndUser(image.getFilename(), user);
    }

    @Test
    void testDeleteImageShouldThrowImageNotFoundException() {
        // given
        String filename = "imageNotFound";

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.existsByFilenameAndUser(filename, user)).thenReturn(false);

        // then
        assertThatThrownBy(() -> imageService.deleteImage(filename, TOKEN))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining("Image " + filename + " does not exist");
    }

    @Test
    void getImagesByDateTimeAndPage() {
        // given
        // same as before for these two lines
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LocalDateTime dateTime = LocalDateTime.now().withNano(0);
        Pageable pageable = PageRequest.of(0, 2);

        // when
        when(jwtService.extractUsername(any())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);
        when(imageRepository.existsByCreatedAtAndUser(dateTime, user)).thenReturn(true);
        when(imageRepository.findByCreatedAtAndUser(dateTime, user, pageable)).thenReturn(List.of(image));

        List<ImageResponse> expected = imageService.getImagesByDateTimeAndPage(dateTime, 0, TOKEN);

        // then
        assertThat(expected).isNotNull();
    }

    @Test
    void getImagesByDateTimeAndPageAndSort() {
        // given

        // when

        // then
    }

    @Test
    void sortAllImages() {
        // given

        // when

        // then
    }
}