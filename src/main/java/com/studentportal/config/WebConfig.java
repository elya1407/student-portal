package com.studentportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Загруженные файлы (аватары, справки, вложения) хранятся в базе данных и отдаются через
 * {@link com.studentportal.controller.FileDownloadController} по адресу /files/{id} —
 * так они не пропадают при перезапуске сервера на хостинге без постоянного диска.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
