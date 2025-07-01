package ru.checkdev.auth.dto;

import lombok.Data;

@Data
public class BindDTO {
    private String email;
    private String password;
    private Long telegramId;
}
