package com.studentportal.service;

import com.studentportal.model.Excuse;
import com.studentportal.model.Student;
import com.studentportal.repository.ExcuseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ExcuseService {
    private final ExcuseRepository excuseRepository;
    private final Path uploadDir;

    public ExcuseService(ExcuseRepository excuseRepository, @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.excuseRepository = excuseRepository;
        this.uploadDir = Path.of(uploadDir);
    }

    public List<Excuse> forStudent(Student student) {
        return excuseRepository.findByStudentOrderByDateFromDesc(student);
    }

    public List<Excuse> all() {
        return excuseRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Excuse create(Student student, LocalDate dateFrom, LocalDate dateTo, String reasonType, String comment, MultipartFile file) {
        if (dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
        Excuse excuse = new Excuse();
        excuse.setStudent(student);
        excuse.setDateFrom(dateFrom);
        excuse.setDateTo(dateTo);
        excuse.setReasonType(reasonType);
        excuse.setComment(comment);

        if (file != null && !file.isEmpty()) {
            attachFile(excuse, file);
        }
        return excuseRepository.save(excuse);
    }

    @Transactional
    public void delete(Long id) {
        excuseRepository.deleteById(id);
    }

    private void attachFile(Excuse excuse, MultipartFile file) {
        try {
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
            String safeName = sanitizeFileName(originalName);
            String savedName = UUID.randomUUID() + "_" + safeName;
            Path target = uploadDir.resolve(savedName);
            file.transferTo(target);
            excuse.setFileName(originalName);
            excuse.setFileUrl("/uploads/" + savedName);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить файл справки", e);
        }
    }

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
