package com.preschool.library.kafkautils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.library.core.AppObjectMapper;
import com.preschool.library.core.exception.NonRetryableException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConsumerAbstract<T> {
    public void executeHandle(String message, Class<T> tClass) {

    }

    private T parseMessage(String message, Class<T> tClass) {
        try {
            ObjectMapper objectMapper = new AppObjectMapper();
        } catch (Exception e) {
            log.error("An error occur, NonRetryableException will be thrown. {}", e.getMessage(), e);
            throw new NonRetryableException();
        }
    }
}
