package com.studentportal.dto;

public record AttendanceStatsDto(long totalLessons, long present, long missed, double percent) {
}
