package com.studentportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ApiAuthService {
    private final String botApiKey;

    public ApiAuthService(@Value("${app.bot-api-key:dev-bot-key-change-me}") String botApiKey) {
        this.botApiKey = botApiKey;
    }

    /**
     * Задел под Telegram-бота: бот будет передавать этот ключ в заголовке X-BOT-API-KEY.
     * Позже можно заменить на JWT или OAuth.
     */
    public void requireBotKey(String key) {
        if (key == null || key.isBlank() || !key.equals(botApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Некорректный API-ключ бота");
        }
    }
}
