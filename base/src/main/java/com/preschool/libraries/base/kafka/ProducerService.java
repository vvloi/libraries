package com.preschool.libraries.base.kafka;

import com.preschool.libraries.base.dto.TrackingRequestDTO;
import com.preschool.libraries.base.common.CommonConstants;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(DefaultKafkaProperties.class)
public class ProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DefaultKafkaProperties defaultKafkaProperties;

    public <T, A> void sendMessage(KafkaMessageMetadata<T, A> messageMetadata) {
        KafkaData<T, A> message =
                new KafkaData<>(
                        messageMetadata.getEvent(),
                        ZonedDateTime.now().toString(),
                        messageMetadata.getData(),
                        messageMetadata.getAdditionalData());
        MessageHeaders headers =
                new MessageHeaders(
                        headers(messageMetadata.getServiceName(), messageMetadata.getXRequestId()));
        kafkaTemplate.send(messageMetadata.getTopic(), new GenericMessage<>(message, headers));
    }

    private Map<String, Object> headers(String serviceName, String xRequestId) {
        Map<String, Object> map = new HashMap<>();
        map.put(
                CommonConstants.X_REQUEST_ID,
                StringUtils.hasText(xRequestId) ? xRequestId : UUID.randomUUID().toString());
        map.put(CommonConstants.SERVICE_NAME, serviceName);
        return map;
    }

    public void sendTrackingMessage(KafkaMessageMetadata<TrackingRequestDTO, Void> messageMetadata) {
        messageMetadata.setEvent(CommonConstants.COLLECT_METRICS);
        messageMetadata.setTopic(defaultKafkaProperties.getMetricsTopic());
        sendMessage(messageMetadata);
    }
}
