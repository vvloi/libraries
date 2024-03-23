package com.preschool.library.kafkautils;

import com.preschool.library.core.ApplicationConstants;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T, A> void sendMessage(KafkaMessageMetadata<T, A> messageMetadata) {
        KafkaData<T, A> message =
                new KafkaData<>(
                        messageMetadata.event(),
                        LocalDateTime.now(),
                        messageMetadata.data(),
                        messageMetadata.additionalData());
        MessageHeaders headers =
                new MessageHeaders(headers(messageMetadata.serviceName(), messageMetadata.xRequestId()));
        kafkaTemplate.send(messageMetadata.topic(), new GenericMessage<>(message, headers));
    }

    private Map<String, Object> headers(String serviceName, String xRequestId) {
        Map<String, Object> map = new HashMap<>();
        map.put(
                ApplicationConstants.X_REQUEST_ID,
                StringUtils.hasText(xRequestId) ? xRequestId : UUID.randomUUID().toString());
        map.put(ApplicationConstants.SERVICE_NAME, serviceName);
        return map;
    }
}
