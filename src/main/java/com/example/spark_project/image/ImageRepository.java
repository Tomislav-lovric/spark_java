package com.example.spark_project.image;

import com.example.spark_project.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    boolean existsByFilenameAndUser(String filename, User user);

    Optional<Image> findByFilenameAndUser(String filename, User user);

    void deleteByFilenameAndUser(String filename, User user);

    List<Image> findByCreatedAtAndUser(LocalDateTime createdAt, User user, Pageable pageable);

    boolean existsByCreatedAtAndUser(LocalDateTime createdAt, User user);

    List<Image> findByUserOrderBySizeAsc(User user);

    List<Image> findByUserOrderBySizeDesc(User user);

    boolean existsByUser(User user);

    List<Image> findByCreatedAtAndUserOrderBySizeAsc(LocalDateTime createdAt, User user, Pageable pageable);

    List<Image> findByCreatedAtAndUserOrderBySizeDesc(LocalDateTime createdAt, User user, Pageable pageable);
}
