package com.example.spark_project.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse {

    private String filename;
    private Long size;
    private LocalDateTime createdAt;
    private String imageLink;
}
