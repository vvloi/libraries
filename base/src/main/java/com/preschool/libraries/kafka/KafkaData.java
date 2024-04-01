package com.preschool.libraries.kafka;

public record KafkaData<T, A>(String event, String eventTime, T data, A additionalData) {}
