package ru.checkdev.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.auth.domain.Notify;
import ru.checkdev.auth.kafka.KafkaNotificationProducer;

/**
 * @author Petr Arsentev (parsentev@yandex.ru)
 * @version $Id$
 * @since 0.1
 */
@Service
@RequiredArgsConstructor
public class Messenger {

    private final KafkaNotificationProducer kafka;

    public void send(Notify notify) {
       kafka.send(notify);
    }

}
