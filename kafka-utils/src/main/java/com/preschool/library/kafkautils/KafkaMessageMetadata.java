package com.preschool.library.kafkautils;

import lombok.Builder;

@Builder
public record KafkaMessageMetadata<T, A>(
        String topic, String event, T data, A additionalData, String serviceName, String xRequestId) {}
