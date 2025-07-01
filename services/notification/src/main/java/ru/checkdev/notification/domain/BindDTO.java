package ru.checkdev.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BindDTO {
    private String email;
    private String password;
    private Long telegramId;
}
