package com.studentportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_windows")
public class AttendanceWindow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer subgroup;

    @Column(name = "week_type", nullable = false, length = 20)
    private String weekType;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closes_at", nullable = false)
    private LocalDateTime closesAt;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "opened_by")
    private String openedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSubgroup() { return subgroup; }
    public void setSubgroup(Integer subgroup) { this.subgroup = subgroup; }

    public String getWeekType() { return weekType; }
    public void setWeekType(String weekType) { this.weekType = weekType; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public LocalDateTime getClosesAt() { return closesAt; }
    public void setClosesAt(LocalDateTime closesAt) { this.closesAt = closesAt; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getOpenedBy() { return openedBy; }
    public void setOpenedBy(String openedBy) { this.openedBy = openedBy; }
}
