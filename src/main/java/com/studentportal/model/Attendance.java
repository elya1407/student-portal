package com.studentportal.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(name = "uq_attendance_student_date", columnNames = {"student_id", "date"}))
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(name = "week_type", nullable = false, length = 20)
    private String weekType;

    @Column(nullable = false)
    private Integer subgroup;

    /** null = отметился сам, иначе логин старосты, который отметил вручную */
    @Column(name = "marked_by")
    private String markedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getWeekType() { return weekType; }
    public void setWeekType(String weekType) { this.weekType = weekType; }

    public Integer getSubgroup() { return subgroup; }
    public void setSubgroup(Integer subgroup) { this.subgroup = subgroup; }

    public String getMarkedBy() { return markedBy; }
    public void setMarkedBy(String markedBy) { this.markedBy = markedBy; }
}
