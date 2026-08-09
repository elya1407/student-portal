package com.studentportal.dto;

import com.studentportal.model.Schedule;
import java.time.LocalTime;

public record ScheduleDto(
        Long id,
        String weekType,
        String dayOfWeek,
        Integer subgroup,
        LocalTime startTime,
        LocalTime endTime,
        String discipline
) {
    public static ScheduleDto from(Schedule schedule) {
        return new ScheduleDto(
                schedule.getId(),
                schedule.getWeekType(),
                schedule.getDayOfWeek(),
                schedule.getSubgroup(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getDiscipline()
        );
    }
}
