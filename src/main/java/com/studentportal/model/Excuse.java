package com.studentportal.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Уважительная причина отсутствия: даты, тип причины и прикреплённый файл (справка/приказ).
 * Видна самому студенту (личный кабинет), старосте (админка) — при необходимости
 * старосте есть что показать преподавателю.
 */
@Entity
@Table(name = "excuses")
public class Excuse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    /** больничный / приказ / семейные обстоятельства / другое */
    @Column(name = "reason_type", nullable = false, length = 30)
    private String reasonType;

    @Column(length = 1000)
    private String comment;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public String getReasonType() { return reasonType; }
    public void setReasonType(String reasonType) { this.reasonType = reasonType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
