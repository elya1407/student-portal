package com.studentportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Раздача файлов из /uploads теперь идёт через явный
 * {@link com.studentportal.controller.FileDownloadController}, а не через
 * стандартный ResourceHandler — так надёжнее работает с именами файлов
 * с пробелами/кириллицей и гарантированно отдаёт Content-Disposition для скачивания.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
