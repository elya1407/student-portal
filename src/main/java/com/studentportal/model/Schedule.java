package com.studentportal.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedule",
       uniqueConstraints = @UniqueConstraint(name = "uq_schedule_week_day_subgroup", columnNames = {"week_type", "day_of_week", "subgroup"}))
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_type", nullable = false, length = 20)
    private String weekType;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    @Column(nullable = false)
    private Integer subgroup;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String discipline;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWeekType() { return weekType; }
    public void setWeekType(String weekType) { this.weekType = weekType; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getSubgroup() { return subgroup; }
    public void setSubgroup(Integer subgroup) { this.subgroup = subgroup; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }
}
