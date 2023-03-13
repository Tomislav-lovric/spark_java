package com.example.spark_project.image;

import com.example.spark_project.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "image"
)
public class Image {

    @Id
    @SequenceGenerator(
            name = "image_id_sequence",
            sequenceName = "image_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "image_id_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "filename",
            nullable = false
    )
    private String filename;

    @Column(
            name = "mime_type",
            nullable = false
    )
    private String mimeType;

    @Column(
            name = "data",
            nullable = false
    )
    private byte[] data;

    //in bytes
    @Column(
            name = "size",
            nullable = false
    )
    private Long size;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(
            name = "user_email",
            nullable = false,
            referencedColumnName = "email",
            foreignKey = @ForeignKey(
                    name = "user_email_image_fk"
            )
    )
    private User user;

}
