package com.example.spark_project.image;

import com.example.spark_project.security.JwtService;
import com.example.spark_project.user.Role;
import com.example.spark_project.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageService imageService;

    @MockBean
    private JwtService jwtService;

    private final String jwtToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private static final String END_POINT_PATH = "/api/v1/image";

    private MockMultipartFile file;
    private ImageResponse imageResponse;
    private Image image;
    private String imageLink;

    @BeforeEach
    void setUp() {
        String resetToken = UUID.randomUUID().toString().replaceAll("_", "").substring(0, 32);
        User user = User.builder()
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
                .createdAt(LocalDateTime.now().withNano(0))
                .user(user)
                .build();

        file = new MockMultipartFile(
                "file",
                "Earth.gif",
                "image/gif",
                new byte[] {0x00, 0x01, 0x02, 0x03}
        );

        imageLink = "http://localhost/api/v1/image/" + file.getOriginalFilename();

        imageResponse = ImageResponse.builder()
                .filename(image.getFilename())
                .size(image.getSize())
                .createdAt(image.getCreatedAt())
                .imageLink(imageLink)
                .build();
    }

    @Test
    void getImage() throws Exception {
        // given
        String filename = "Earth.gif";

        // when
        when(imageService.getImage(filename, jwtToken)).thenReturn(image);

        // then
        MvcResult mvcResult = mockMvc.perform(get(END_POINT_PATH + "/{filename}", filename)
                .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(image.getMimeType()))
                .andExpect(content().bytes(image.getData()))
                .andReturn();

        byte[] responseBytes = mvcResult.getResponse().getContentAsByteArray();
        assertThat(responseBytes).isEqualTo(image.getData());
    }

    @Test
    void searchImage() throws Exception {
        // given
        String filename = "Earth.gif";

        // when
        when(imageService.getImage(filename, jwtToken)).thenReturn(image);

        // then
        MvcResult mvcResult = mockMvc.perform(get(END_POINT_PATH + "/search")
                .header("Authorization", jwtToken)
                .param("filename", filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(image.getMimeType()))
                .andExpect(content().bytes(image.getData()))
                .andReturn();

        byte[] responseBytes = mvcResult.getResponse().getContentAsByteArray();
        assertThat(responseBytes).isEqualTo(image.getData());
    }

    @Test
    void uploadImage() throws Exception {
        // when
        when(imageService.uploadImage(file, jwtToken)).thenReturn(imageResponse);

        // then
        mockMvc.perform(multipart(END_POINT_PATH + "/upload")
                .file(file)
                .contentType(MediaType.IMAGE_GIF)
                .header("Authorization", jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value(image.getFilename()))
                .andExpect(jsonPath("$.size").value(image.getSize()))
                .andExpect(jsonPath("$.createdAt").value(image.getCreatedAt().toString()))
                .andExpect(jsonPath("$.imageLink").value(imageLink));
    }

    @Test
    void uploadMulti() throws Exception {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "Earth.gif",
                "image/gif",
                new byte[] {0x00, 0x01, 0x02, 0x03}
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "Earth2.gif",
                "image/gif",
                new byte[] {0x00, 0x01, 0x02, 0x03}
        );

        String imageLink1 = "http://localhost/api/v1/image/" + file1.getOriginalFilename();
        String imageLink2 = "http://localhost/api/v1/image/" + file2.getOriginalFilename();

        ImageResponse imageResponse1 = ImageResponse.builder()
                .filename(file1.getOriginalFilename())
                .size(file1.getSize())
                .createdAt(LocalDateTime.now().withNano(0))
                .imageLink(imageLink1)
                .build();

        ImageResponse imageResponse2 = ImageResponse.builder()
                .filename(file2.getOriginalFilename())
                .size(file2.getSize())
                .createdAt(LocalDateTime.now().withNano(0))
                .imageLink(imageLink2)
                .build();

        List<ImageResponse> expectedImageResponseList = Arrays.asList(imageResponse, imageResponse2);

        // when
        when(imageService.uploadImage(any(MockMultipartFile.class), eq(jwtToken)))
                .thenReturn(imageResponse1, imageResponse2);

        // then
        MvcResult result = mockMvc.perform(multipart(END_POINT_PATH + "/upload_multi")
                .file("files", file1.getBytes())
                .file("files", file2.getBytes())
                .header("Authorization", jwtToken))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        List<ImageResponse> actualImageResponseList = objectMapper.readValue(responseJson, new TypeReference<List<ImageResponse>>() {});
        assertThat(expectedImageResponseList).isEqualTo(actualImageResponseList);

    }

    @Test
    void changeImage() throws Exception {
        // given
        String filename = "Earth.gif";
        LocalDateTime createdAt = LocalDateTime.now().withNano(0);

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "Earth2.gif",
                "image/gif",
                new byte[] {0x00, 0x01, 0x02, 0x03}
        );

        String imageLink2 = "http://localhost/api/v1/image/" + file2.getOriginalFilename();

        ImageResponse imageResponse2 = ImageResponse.builder()
                .filename(file2.getOriginalFilename())
                .size(file2.getSize())
                .createdAt(createdAt)
                .imageLink(imageLink2)
                .build();

        // when
        when(imageService.changeImage(filename, file2, jwtToken)).thenReturn(imageResponse2);

        // then
        mockMvc.perform(multipart(END_POINT_PATH + "/{filename}", filename)
                .file(file2)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .contentType(MediaType.IMAGE_GIF)
                .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value(file2.getOriginalFilename()))
                .andExpect(jsonPath("$.size").value(file2.getSize()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.imageLink").value(imageLink2));
    }

    @Test
    void deleteImage() {
        // given

        // when

        // then
    }

    @Test
    void getImagesByDateTimeAndPage() {
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