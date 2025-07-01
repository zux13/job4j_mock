package ru.checkdev.auth.dto;

import lombok.Data;

@Data
public class UnbindDTO {
    private String email;
    private String password;
}
