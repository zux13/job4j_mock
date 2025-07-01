package ru.checkdev.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnbindDTO {
    private String email;
    private String password;
}
