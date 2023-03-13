package com.example.spark_project.image;

import com.example.spark_project.exception.FileNotAnImageException;
import com.example.spark_project.exception.ImageAlreadyExistsException;
import com.example.spark_project.exception.ImageNotFoundException;
import com.example.spark_project.exception.InvalidSortOrderException;
import com.example.spark_project.security.JwtService;
import com.example.spark_project.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public Image getImage(String filename, String bearerToken) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        return imageRepository.findByFilenameAndUser(filename, user)
                .orElseThrow(() -> new ImageNotFoundException(
                        "Image " + filename + " does not exist"
                ));
    }

    public ImageResponse uploadImage(MultipartFile file, String bearerToken) throws IOException {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!file.getContentType().startsWith("image")) {
            throw new FileNotAnImageException("File you are trying to upload is not an image");
        }

        if (imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)) {
            throw new ImageAlreadyExistsException("Image with that filename already exists");
        }

        var image = Image.builder()
                .filename(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .data(file.getBytes())
                .size(file.getSize())
                .createdAt(LocalDateTime.now().withNano(0))
                .user(user)
                .build();

        imageRepository.save(image);

        return ImageResponse.builder()
                .filename(image.getFilename())
                .size(file.getSize())
                .createdAt(image.getCreatedAt())
                .imageLink(createImageLink(image.getFilename()))
                .build();
    }

    @Transactional
    public ImageResponse changeImage(String filename, MultipartFile file, String bearerToken) throws IOException {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByFilenameAndUser(filename, user)) {
            throw new ImageNotFoundException("Image " + filename + " does not exist");
        }

        if (!file.getContentType().startsWith("image")) {
            throw new FileNotAnImageException("File you are trying to upload is not an image");
        }

        if (imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)) {
            throw new ImageAlreadyExistsException("Image " + file.getOriginalFilename() + " already exists");
        }

        var optionalImage = imageRepository.findByFilenameAndUser(filename, user);
        var image = optionalImage.get();

        image.setFilename(file.getOriginalFilename());
        image.setMimeType(file.getContentType());
        image.setData(file.getBytes());
        image.setSize(file.getSize());
        image.setCreatedAt(LocalDateTime.now().withNano(0));
        imageRepository.save(image);

        return ImageResponse.builder()
                .filename(image.getFilename())
                .size(image.getSize())
                .createdAt(image.getCreatedAt())
                .imageLink(createImageLink(image.getFilename()))
                .build();
    }

    @Transactional
    public String deleteImage(String filename, String bearerToken) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByFilenameAndUser(filename, user)) {
            throw new ImageNotFoundException("Image " + filename + " does not exist");
        }

        imageRepository.deleteByFilenameAndUser(filename, user);

        return filename + " image deleted";
    }

    private String createImageLink(String filename) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/image/" + filename)
                .replaceQuery(null)
                .toUriString();
    }

    public List<ImageResponse> getImagesByDateTimeAndPage(LocalDateTime date, Integer page, String bearerToken) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByCreatedAtAndUser(date, user)) {
            throw new ImageNotFoundException("No images found");
        }

        Pageable pageable = PageRequest.of(page, 2);
        List<Image> images = imageRepository.findByCreatedAtAndUser(date, user, pageable);

        return images.stream()
                .map(image -> ImageResponse.builder()
                        .filename(image.getFilename())
                        .size(image.getSize())
                        .createdAt(image.getCreatedAt())
                        .imageLink(createImageLink(image.getFilename()))
                        .build()).toList();
    }

    public List<ImageResponse> getImagesByDateTimeAndPageAndSort(LocalDateTime date, Integer page, String bearerToken, String order) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByCreatedAtAndUser(date, user)) {
            throw new ImageNotFoundException("No images found");
        }

        Pageable pageable = PageRequest.of(page, 2);
        List<Image> images;

        if (order.equalsIgnoreCase("asc")) {
            images = imageRepository.findByCreatedAtAndUserOrderBySizeAsc(date, user, pageable);
            return images.stream()
                    .map(image -> ImageResponse.builder()
                            .filename(image.getFilename())
                            .size(image.getSize())
                            .createdAt(image.getCreatedAt())
                            .imageLink(createImageLink(image.getFilename()))
                            .build()).toList();
        } else if (order.equalsIgnoreCase("desc")) {
            images = imageRepository.findByCreatedAtAndUserOrderBySizeDesc(date, user, pageable);
            return images.stream()
                    .map(image -> ImageResponse.builder()
                            .filename(image.getFilename())
                            .size(image.getSize())
                            .createdAt(image.getCreatedAt())
                            .imageLink(createImageLink(image.getFilename()))
                            .build()).toList();
        }

        throw new InvalidSortOrderException("Invalid sort order. Only use ASC or DESC");
    }

    public List<ImageResponse> sortAllImages(String order, String bearerToken) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByUser(user)) {
            throw new ImageNotFoundException("No images found");
        }
        List<Image> images;

        if (order.equalsIgnoreCase("asc")) {
            images = imageRepository.findByUserOrderBySizeAsc(user);
            return images.stream()
                    .map(image -> ImageResponse.builder()
                            .filename(image.getFilename())
                            .size(image.getSize())
                            .createdAt(image.getCreatedAt())
                            .imageLink(createImageLink(image.getFilename()))
                            .build()).toList();
        } else if (order.equalsIgnoreCase("desc")) {
            images = imageRepository.findByUserOrderBySizeDesc(user);
            return images.stream()
                    .map(image -> ImageResponse.builder()
                            .filename(image.getFilename())
                            .size(image.getSize())
                            .createdAt(image.getCreatedAt())
                            .imageLink(createImageLink(image.getFilename()))
                            .build()).toList();
        }

        throw new InvalidSortOrderException("Invalid sort order. Only use ASC or DESC");
    }
}
