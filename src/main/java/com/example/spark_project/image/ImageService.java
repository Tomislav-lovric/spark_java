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
        //extract username (email) out of token and fetch user out of db with it
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        //then return image if it exists else throw our custom exception
        return imageRepository.findByFilenameAndUser(filename, user)
                .orElseThrow(() -> new ImageNotFoundException(
                        "Image " + filename + " does not exist"
                ));
    }

    public ImageResponse uploadImage(MultipartFile file, String bearerToken) throws IOException {
        //extract username (email) out of token and fetch user out of db with it
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        //check if file provided is image, if not throw exp
        if (!file.getContentType().startsWith("image")) {
            throw new FileNotAnImageException("File you are trying to upload is not an image");
        }

        //check if image exists, if not throw exp
        if (imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)) {
            throw new ImageAlreadyExistsException("Image with that filename already exists");
        }

        //if it does exist, build image and save it to our db
        var image = Image.builder()
                .filename(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .data(file.getBytes())
                .size(file.getSize())
                .createdAt(LocalDateTime.now().withNano(0))
                .user(user)
                .build();

        imageRepository.save(image);

        //then build our response and return it to user
        //our response will contain link which user can use to
        //get image
        return ImageResponse.builder()
                .filename(image.getFilename())
                .size(file.getSize())
                .createdAt(image.getCreatedAt())
                .imageLink(createImageLink(image.getFilename()))
                .build();
    }

    @Transactional
    public ImageResponse changeImage(String filename, MultipartFile file, String bearerToken) throws IOException {
        //same as before
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByFilenameAndUser(filename, user)) {
            throw new ImageNotFoundException("Image " + filename + " does not exist");
        }

        if (!file.getContentType().startsWith("image")) {
            throw new FileNotAnImageException("File you are trying to upload is not an image");
        }

        //we also check if new image already exists in our db
        //if it does throw exp
        if (imageRepository.existsByFilenameAndUser(file.getOriginalFilename(), user)) {
            throw new ImageAlreadyExistsException("Image " + file.getOriginalFilename() + " already exists");
        }

        //if everything is ok get image
        var optionalImage = imageRepository.findByFilenameAndUser(filename, user);
        var image = optionalImage.get();

        //and change it
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

    //method for creating link which user uses to get an image
    //simple method which gets current url and replaces its path
    //with our new provided path. We also get rid of all query strings (we need
    //this part because of mappings which require query strings)
    private String createImageLink(String filename) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/image/" + filename)
                .replaceQuery(null)
                .toUriString();
    }

    public List<ImageResponse> getImagesByDateTimeAndPage(LocalDateTime date, Integer page, String bearerToken) {
        //pretty much same stuff as before
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByCreatedAtAndUser(date, user)) {
            throw new ImageNotFoundException("No images found");
        }

        //todo maybe we should add check for page as well???

        //we then create pageable, so we can create paging
        //(used 2 items per page for testing purposes)
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

    //pretty much same method as above only with sorting
    public List<ImageResponse> getImagesByDateTimeAndPageAndSort(LocalDateTime date, Integer page, String bearerToken, String order) {
        String username = jwtService.extractUsername(bearerToken.substring(7));
        var user = userRepository.findUserByEmail(username);

        if (!imageRepository.existsByCreatedAtAndUser(date, user)) {
            throw new ImageNotFoundException("No images found");
        }

        Pageable pageable = PageRequest.of(page, 2);
        List<Image> images;

        //we check what kind of sort order user wants (asc or desc)
        //and return the response based on provided order
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

        //of sort order is neither asc nor desc throw custom error
        throw new InvalidSortOrderException("Invalid sort order. Only use ASC or DESC");
    }

    //same as above sorting only for all images
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
