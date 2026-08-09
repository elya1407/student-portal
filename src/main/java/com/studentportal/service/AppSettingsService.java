package com.studentportal.service;

import com.studentportal.model.AppSettings;
import com.studentportal.repository.AppSettingsRepository;
import com.studentportal.util.WeekTypeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AppSettingsService {
    private final AppSettingsRepository appSettingsRepository;

    public AppSettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    /** Подхватываем сохранённую опорную дату при старте приложения. */
    @PostConstruct
    public void init() {
        appSettingsRepository.findById(1L).ifPresent(s -> {
            if (s.getWeekAnchorDate() != null && s.getWeekAnchorType() != null) {
                WeekTypeUtil.setAnchor(s.getWeekAnchorDate(), s.getWeekAnchorType());
            }
        });
    }

    @Transactional
    public void setAnchor(LocalDate date, String weekType) {
        if (!WeekTypeUtil.NUMERATOR.equals(weekType) && !WeekTypeUtil.DENOMINATOR.equals(weekType)) {
            throw new IllegalArgumentException("Неделя должна быть 'числитель' или 'знаменатель'");
        }
        AppSettings settings = appSettingsRepository.findById(1L).orElseGet(() -> {
            AppSettings s = new AppSettings();
            s.setId(1L);
            return s;
        });
        settings.setWeekAnchorDate(date);
        settings.setWeekAnchorType(weekType);
        appSettingsRepository.save(settings);
        WeekTypeUtil.setAnchor(date, weekType);
    }

    public LocalDate getAnchorDate() {
        return WeekTypeUtil.getAnchorDate();
    }

    public String getAnchorType() {
        return WeekTypeUtil.getAnchorType();
    }
}
