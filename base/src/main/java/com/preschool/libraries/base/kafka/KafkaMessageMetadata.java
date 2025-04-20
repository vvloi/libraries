package com.preschool.libraries.base.kafka;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class KafkaMessageMetadata<T, A> {
  private String topic;
  private String event;
  private T data;
  private A additionalData;
  private String serviceName;
  private String xRequestId;
}
