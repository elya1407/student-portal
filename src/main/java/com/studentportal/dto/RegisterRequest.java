package com.studentportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RegisterRequest {
    @NotBlank(message = "Введите ФИО")
    private String fullName;

    @NotBlank(message = "Введите номер зачётки")
    private String recordBookNumber;

    private String phone;

    @Email(message = "Некорректная почта")
    private String email;

    private LocalDate birthDate;

    @NotBlank(message = "Введите пароль")
    @Size(min = 6, message = "Пароль минимум 6 символов")
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(String recordBookNumber) { this.recordBookNumber = recordBookNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
