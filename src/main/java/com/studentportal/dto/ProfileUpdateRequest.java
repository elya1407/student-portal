package com.studentportal.dto;

import jakarta.validation.constraints.Email;
import java.time.LocalDate;

public class ProfileUpdateRequest {
    private String phone;

    @Email(message = "Некорректная почта")
    private String email;

    private LocalDate birthDate;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
}
