package com.studentportal.service;

import com.studentportal.model.StoredFile;
import com.studentportal.repository.StoredFileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Центральное место для сохранения загруженных файлов (аватары, справки, вложения к постам).
 * Файлы хранятся как байты в таблице stored_files, а не на диске — это переживает
 * перезапуски и "засыпание" контейнера на бесплатном хостинге без постоянного диска.
 *
 * Ссылка на файл в остальной части приложения выглядит как "/files/{id}" — эти ссылки
 * обрабатывает FileDownloadController, читая байты из базы по id.
 */
@Service
public class FileStorageService {
    private final StoredFileRepository storedFileRepository;

    public FileStorageService(StoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    /** Сохраняет файл в базе и возвращает публичную ссылку вида "/files/{id}" */
    @Transactional
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран");
        }
        try {
            String originalName = file.getOriginalFilename() == null
                    ? "file"
                    : Path.of(file.getOriginalFilename()).getFileName().toString();

            StoredFile stored = new StoredFile();
            stored.setOriginalName(originalName);
            stored.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            stored.setData(file.getBytes());
            stored = storedFileRepository.save(stored);

            return "/files/" + stored.getId();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить файл", e);
        }
    }

    /** Оригинальное имя файла — для отображения в интерфейсе и заголовка при скачивании */
    public String originalNameOf(MultipartFile file) {
        return file.getOriginalFilename() == null
                ? "file"
                : Path.of(file.getOriginalFilename()).getFileName().toString();
    }
}
