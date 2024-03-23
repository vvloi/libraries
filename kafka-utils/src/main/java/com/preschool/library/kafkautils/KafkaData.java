package com.preschool.library.kafkautils;

public record KafkaData<T, A>(String event, String eventTime, T data, A additionalData) {}
