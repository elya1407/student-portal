package com.studentportal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class FileDownloadController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Path base = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(filename).normalize();

        // защита от выхода за пределы папки uploads через ../
        if (!target.startsWith(base)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(target);
        String contentType;
        try {
            contentType = Files.probeContentType(target);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
