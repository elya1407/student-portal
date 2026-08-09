package com.studentportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "record_book_number", nullable = false, unique = true, length = 20)
    private String recordBookNumber;

    @Min(1)
    @Max(3)
    @Column(nullable = false)
    private Integer subgroup;

    @Column(length = 20)
    private String phone;

    @Email
    @Column(length = 100)
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "theme_color", length = 20, nullable = false)
    private String themeColor = "peach";

    public boolean isRegistered() {
        return passwordHash != null && !passwordHash.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(String recordBookNumber) { this.recordBookNumber = recordBookNumber; }

    public Integer getSubgroup() { return subgroup; }
    public void setSubgroup(Integer subgroup) { this.subgroup = subgroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }
}
