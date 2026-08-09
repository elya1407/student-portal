package com.studentportal.service;

import com.studentportal.model.Post;
import com.studentportal.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final Path uploadDir;

    public PostService(PostRepository postRepository, @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.postRepository = postRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> findVisibleForSubgroup(Integer subgroup) {
        return postRepository.findVisibleForSubgroup(subgroup);
    }

    @Transactional
    public Post create(String title, String content, Integer forSubgroup, MultipartFile file, String createdBy) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setForSubgroup(forSubgroup);
        post.setCreatedAt(LocalDateTime.now());
        post.setCreatedBy(createdBy);

        if (file != null && !file.isEmpty()) {
            attachFile(post, file);
        }
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, String title, String content, Integer forSubgroup) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пост не найден"));
        post.setTitle(title);
        post.setContent(content);
        post.setForSubgroup(forSubgroup);
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    private void attachFile(Post post, MultipartFile file) {
        try {
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
            String safeName = sanitizeFileName(originalName);
            String savedName = UUID.randomUUID() + "_" + safeName;
            Path target = uploadDir.resolve(savedName);
            file.transferTo(target);
            post.setFileName(originalName);
            post.setFileUrl("/uploads/" + savedName);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить файл", e);
        }
    }

    /**
     * Оставляет только безопасные символы в имени файла на диске (латиница, цифры, . _ -),
     * иначе кириллица/пробелы в имени иногда ломают отдачу файла через URL (404 при скачивании).
     * Исходное красивое имя всё равно показывается пользователю через fileName.
     */
    private String sanitizeFileName(String originalName) {
        String dotless = originalName.replaceAll("[\\\\/]", "_");
        String safe = dotless.replaceAll("[^A-Za-z0-9._-]", "_");
        safe = safe.replaceAll("_+", "_");
        if (safe.isBlank() || safe.equals(".") || safe.equals("_")) {
            safe = "file";
        }
        return safe;
    }
}
