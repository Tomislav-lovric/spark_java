package com.example.spark_project.image;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/image")
@RequiredArgsConstructor
public class ImageController {
    //simple image controller, only thing to note here
    //is we always get token out of header which we will use in
    //our image service for our operations

    private final ImageService service;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String filename,
            @RequestHeader("Authorization") String bearerToken
    ) {
        var image = service.getImage(filename, bearerToken);
        var body = new ByteArrayResource(image.getData());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, image.getMimeType())
                .body(body);
    }

    @GetMapping("/search")
    public ResponseEntity<Resource> searchImage(
            @RequestParam String filename,
            @RequestHeader("Authorization") String bearerToken
    ) {
        var image = service.getImage(filename, bearerToken);
        var body = new ByteArrayResource(image.getData());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, image.getMimeType())
                .body(body);
    }

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestPart MultipartFile file,
            @RequestHeader("Authorization") String bearerToken
            ) throws IOException {
        return ResponseEntity.ok(service.uploadImage(file, bearerToken));
    }

    @PostMapping("/upload_multi")
    public ResponseEntity<List<ImageResponse>> uploadMulti(
            @RequestPart List<MultipartFile> files,
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(files.stream()
                .map(file -> {
                    try {
                        return service.uploadImage(file, bearerToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()));
    }

    @PutMapping("/{filename}")
    public ResponseEntity<ImageResponse> changeImage(
            @PathVariable String filename,
            @RequestPart MultipartFile file,
            @RequestHeader("Authorization") String bearerToken
    ) throws IOException {
        return ResponseEntity.ok(service.changeImage(filename, file, bearerToken));
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<String> deleteImage(
            @PathVariable String filename,
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(service.deleteImage(filename, bearerToken));
    }

    //todo add search, so user can search for one photo by its name
    @GetMapping("/")
    public ResponseEntity<List<ImageResponse>> getImagesByDateTimeAndPage(
            @RequestParam LocalDateTime date,
            @RequestParam Integer page,
            @RequestParam(required = false) String order,
            @RequestHeader("Authorization") String bearerToken
            ) {
        if (order == null) {
            return ResponseEntity.ok(service.getImagesByDateTimeAndPage(date, page, bearerToken));
        }
        return ResponseEntity.ok(service.getImagesByDateTimeAndPageAndSort(date, page, bearerToken, order));
    }

    @GetMapping("/sort/{order}")
    public ResponseEntity<List<ImageResponse>> sortAllImages(
            @PathVariable String order,
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(service.sortAllImages(order, bearerToken));
    }

}
