package com.studentportal.dto;

import com.studentportal.model.Student;
import java.time.LocalDate;

public record StudentDto(
        Long id,
        String fullName,
        String recordBookNumber,
        Integer subgroup,
        String phone,
        String email,
        LocalDate birthDate,
        Long telegramId,
        Boolean active
) {
    public static StudentDto from(Student student) {
        return new StudentDto(
                student.getId(),
                student.getFullName(),
                student.getRecordBookNumber(),
                student.getSubgroup(),
                student.getPhone(),
                student.getEmail(),
                student.getBirthDate(),
                student.getTelegramId(),
                student.getActive()
        );
    }
}
