package ru.checkdev.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribeTopicDTO {
    private int userId;
    private int topicId;
}
