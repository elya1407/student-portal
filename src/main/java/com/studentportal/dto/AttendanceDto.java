package com.studentportal.dto;

import com.studentportal.model.Attendance;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceDto(
        Long id,
        String fullName,
        String recordBookNumber,
        LocalDate date,
        LocalTime time,
        String weekType,
        Integer subgroup
) {
    public static AttendanceDto from(Attendance attendance) {
        return new AttendanceDto(
                attendance.getId(),
                attendance.getStudent().getFullName(),
                attendance.getStudent().getRecordBookNumber(),
                attendance.getDate(),
                attendance.getTime(),
                attendance.getWeekType(),
                attendance.getSubgroup()
        );
    }
}
