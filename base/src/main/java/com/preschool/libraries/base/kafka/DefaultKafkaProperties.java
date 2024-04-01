package com.preschool.libraries.base.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.default-kafka")
@Getter
@Setter
public class DefaultKafkaProperties {
    private String server;
    private String metricsTopic;
}
