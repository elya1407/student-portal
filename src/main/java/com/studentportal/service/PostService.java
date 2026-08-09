package com.studentportal.service;

import com.studentportal.model.Post;
import com.studentportal.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    public PostService(PostRepository postRepository, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
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
            post.setFileName(fileStorageService.originalNameOf(file));
            post.setFileUrl(fileStorageService.store(file));
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
}
