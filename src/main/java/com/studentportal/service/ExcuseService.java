package com.studentportal.service;

import com.studentportal.model.Excuse;
import com.studentportal.model.Student;
import com.studentportal.repository.ExcuseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExcuseService {
    private final ExcuseRepository excuseRepository;
    private final FileStorageService fileStorageService;

    public ExcuseService(ExcuseRepository excuseRepository, FileStorageService fileStorageService) {
        this.excuseRepository = excuseRepository;
        this.fileStorageService = fileStorageService;
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
            excuse.setFileName(fileStorageService.originalNameOf(file));
            excuse.setFileUrl(fileStorageService.store(file));
        }
        return excuseRepository.save(excuse);
    }

    @Transactional
    public void delete(Long id) {
        excuseRepository.deleteById(id);
    }
}
