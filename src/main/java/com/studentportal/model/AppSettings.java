package com.studentportal.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "app_settings")
public class AppSettings {
    @Id
    private Long id = 1L;

    @Column(name = "week_anchor_date")
    private LocalDate weekAnchorDate;

    @Column(name = "week_anchor_type", length = 20)
    private String weekAnchorType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getWeekAnchorDate() { return weekAnchorDate; }
    public void setWeekAnchorDate(LocalDate weekAnchorDate) { this.weekAnchorDate = weekAnchorDate; }

    public String getWeekAnchorType() { return weekAnchorType; }
    public void setWeekAnchorType(String weekAnchorType) { this.weekAnchorType = weekAnchorType; }
}
