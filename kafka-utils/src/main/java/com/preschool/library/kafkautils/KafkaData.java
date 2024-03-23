package com.preschool.library.kafkautils;

import java.time.LocalDateTime;

public record KafkaData<T, A>(String event, LocalDateTime eventTime, T data, A additionalData) {}
