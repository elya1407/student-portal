package com.studentportal.controller;

import com.studentportal.model.StoredFile;
import com.studentportal.repository.StoredFileRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

/**
 * Отдаёт файлы, загруженные через сайт (аватары, справки, вложения к постам).
 * Файлы хранятся в базе данных (таблица stored_files) — не на диске сервера,
 * поэтому переживают перезапуск/засыпание контейнера на бесплатном хостинге.
 */
@Controller
public class FileDownloadController {

    private final StoredFileRepository storedFileRepository;

    public FileDownloadController(StoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        StoredFile file = storedFileRepository.findById(id).orElse(null);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(file.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String encodedName = java.net.URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName)
                .body(file.getData());
    }
}
