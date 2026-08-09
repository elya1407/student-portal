package com.studentportal.service;

import com.studentportal.dto.PasswordChangeRequest;
import com.studentportal.dto.ProfileUpdateRequest;
import com.studentportal.dto.RegisterRequest;
import com.studentportal.model.Student;
import com.studentportal.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder,
                          FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public Student findByRecordBook(String recordBookNumber) {
        return studentRepository.findByRecordBookNumber(normalize(recordBookNumber))
                .orElseThrow(() -> new IllegalArgumentException("Студент с такой зачёткой не найден"));
    }

    public List<Student> findAll() {
        return studentRepository.findAll().stream()
                .sorted((a, b) -> {
                    int s = Integer.compare(a.getSubgroup(), b.getSubgroup());
                    return s != 0 ? s : a.getFullName().compareToIgnoreCase(b.getFullName());
                })
                .toList();
    }

    public List<Student> findBySubgroup(Integer subgroup) {
        if (subgroup == null) return findAll();
        return studentRepository.findBySubgroupOrderByFullName(subgroup);
    }

    public List<Student> findActiveBySubgroup(Integer subgroup) {
        return studentRepository.findBySubgroupAndActiveTrueOrderByFullName(subgroup);
    }

    @Transactional
    public Student register(RegisterRequest request) {
        Student student = findByRecordBook(request.getRecordBookNumber());

        if (!Boolean.TRUE.equals(student.getActive())) {
            throw new IllegalArgumentException("Учётная запись деактивирована. Обратитесь к старосте.");
        }
        if (student.isRegistered()) {
            throw new IllegalArgumentException("Эта зачётка уже зарегистрирована. Войдите или обратитесь к старосте.");
        }
        if (!normalizeName(student.getFullName()).equals(normalizeName(request.getFullName()))) {
            throw new IllegalArgumentException("ФИО не совпадает со списком группы. Проверьте написание или обратитесь к старосте.");
        }

        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setBirthDate(request.getBirthDate());
        student.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        student.setRegisteredAt(LocalDateTime.now());
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateProfile(Student student, ProfileUpdateRequest request) {
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setBirthDate(request.getBirthDate());
        return studentRepository.save(student);
    }

    @Transactional
    public void changePassword(Student student, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), student.getPasswordHash())) {
            throw new IllegalArgumentException("Старый пароль указан неверно");
        }
        student.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        studentRepository.save(student);
    }

    @Transactional
    public Student adminUpdate(Long id, String fullName, String phone, String email, Integer subgroup, String recordBookNumber) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        if (fullName != null && !fullName.isBlank()) {
            student.setFullName(fullName.trim());
        }
        student.setPhone(phone);
        student.setEmail(email);
        if (subgroup != null && subgroup >= 1 && subgroup <= 3) {
            student.setSubgroup(subgroup);
        }
        if (recordBookNumber != null && !recordBookNumber.isBlank()) {
            String normalized = recordBookNumber.trim();
            if (!normalized.equals(student.getRecordBookNumber())) {
                studentRepository.findByRecordBookNumber(normalized).ifPresent(other -> {
                    throw new IllegalArgumentException("Такой номер зачётки уже используется другим студентом");
                });
                student.setRecordBookNumber(normalized);
            }
        }
        return studentRepository.save(student);
    }

    @Transactional
    public Student adminCreate(String fullName, String recordBookNumber, Integer subgroup) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Укажите ФИО студента");
        }
        if (recordBookNumber == null || recordBookNumber.isBlank()) {
            throw new IllegalArgumentException("Укажите номер зачётки");
        }
        String normalized = recordBookNumber.trim();
        studentRepository.findByRecordBookNumber(normalized).ifPresent(other -> {
            throw new IllegalArgumentException("Такой номер зачётки уже используется");
        });
        if (subgroup == null || subgroup < 1 || subgroup > 3) {
            throw new IllegalArgumentException("Подгруппа должна быть от 1 до 3");
        }
        Student student = new Student();
        student.setFullName(fullName.trim());
        student.setRecordBookNumber(normalized);
        student.setSubgroup(subgroup);
        student.setActive(true);
        return studentRepository.save(student);
    }

    @Transactional
    public void updateTheme(Student student, String themeColor) {
        student.setThemeColor(themeColor);
        studentRepository.save(student);
    }

    @Transactional
    public void updateAvatarUrl(Student student, String avatarUrl) {
        student.setAvatarUrl(avatarUrl);
        studentRepository.save(student);
    }

    @Transactional
    public void uploadAvatar(Student student, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Аватар должен быть изображением (jpg, png, webp)");
        }
        student.setAvatarUrl(fileStorageService.store(file));
        studentRepository.save(student);
    }

    @Transactional
    public String adminResetPassword(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        String newPassword = generateTempPassword();
        student.setPasswordHash(passwordEncoder.encode(newPassword));
        if (student.getRegisteredAt() == null) {
            student.setRegisteredAt(LocalDateTime.now());
        }
        studentRepository.save(student);
        return newPassword;
    }

    private String generateTempPassword() {
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        student.setActive(active);
        studentRepository.save(student);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeName(String value) {
        return normalize(value).replaceAll("\\s+", " ").toLowerCase();
    }
}
