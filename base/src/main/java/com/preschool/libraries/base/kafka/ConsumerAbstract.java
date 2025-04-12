package com.preschool.libraries.base.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.libraries.base.common.AppObjectMapper;
import com.preschool.libraries.base.common.CommonConstants;
import com.preschool.libraries.base.context.SensitiveContext;
import com.preschool.libraries.base.dto.TrackingRequestDTO;
import com.preschool.libraries.base.enumeration.RequestType;
import com.preschool.libraries.base.exception.ApplicationException;
import com.preschool.libraries.base.exception.LibraryErrorCode;
import com.preschool.libraries.base.exception.NonRetryableException;
import com.preschool.libraries.base.processor.SensitiveProcessor;
import com.preschool.libraries.base.properties.SensitiveConfigProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Slf4j
@EnableConfigurationProperties(DefaultKafkaProperties.class)
public abstract class ConsumerAbstract<T> {
  private ObjectMapper objectMapper;
  private ProducerService producerService;
  private DefaultKafkaProperties defaultKafkaProperties;
  private SensitiveConfigProperties sensitiveConfigProperties;

  public ConsumerAbstract() {
    Assert.notNull(
        defaultKafkaProperties.getMetricsTopic(),
        "MUST register metrics-topic when use kafka-utils library");
  }

  @Autowired
  public void setObjectMapper(AppObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setProducerService(ProducerService producerService) {
    this.producerService = producerService;
  }

  @Autowired
  public void setDefaultKafkaProperties(DefaultKafkaProperties defaultKafkaProperties) {
    this.defaultKafkaProperties = defaultKafkaProperties;
  }

  @Autowired
  public void setSensitiveConfigProperties(SensitiveConfigProperties sensitiveConfigProperties) {
    this.sensitiveConfigProperties = sensitiveConfigProperties;
  }

  public void executeHandle(String message, Class<T> tClass, Map<String, String> headers) {
    T data = parseMessage(message, tClass);

    SensitiveContext.setContext(
        SensitiveContext.SensitiveConfig.builder()
            .removeFields(sensitiveConfigProperties.getRemoveFields())
            .build());
    Optional.of(data)
        .map(this::validate)
        .flatMap(
            validationErrors -> {
              log.error(
                  "Having some errors [{}] when validating message data",
                  String.join(",", validationErrors));
              sendTrackingTopic(
                  message,
                  headers,
                  LibraryErrorCode.VALIDATION_KAFKA_MESSAGE_ERROR.name(),
                  StringUtils.toStringArray(validationErrors));
              return Optional.empty();
            })
        .or(
            () -> {
              log.info(
                  "Start process for message has request id [{}]",
                  headers.get(CommonConstants.X_REQUEST_ID));
              execute(message, data, headers);
              return Optional.empty();
            });
  }

  private T parseMessage(String message, Class<T> tClass) {
    T data = null;
    try {
      data = objectMapper.readValue(message, tClass);
    } catch (Exception e) {
      log.error("An error occur, NonRetryableException will be thrown. {}", e.getMessage(), e);
      throw new NonRetryableException();
    }

    return data;
  }

  private List<String> validate(T data) {
    try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = validatorFactory.getValidator();
      Set<ConstraintViolation<T>> violations = validator.validate(data);
      List<String> errorMessages = null;
      if (!violations.isEmpty()) {
        errorMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
      }

      return errorMessages;
    }
  }

  private void sendTrackingTopic(
      String message,
      Map<String, String> headers,
      String responseStatus,
      String... validationErrors) {
    if (Objects.equals(defaultKafkaProperties.getMetricsTopic(), headers.get(KafkaHeaders.TOPIC))) {
      return;
    }

    TrackingRequestDTO trackingRequestDTO =
        buildTrackingRequest(message, headers, responseStatus, validationErrors);
    KafkaMessageMetadata<TrackingRequestDTO, Void> kafkaMessageMetadata =
        KafkaMessageMetadata.<TrackingRequestDTO, Void>builder()
            .data(trackingRequestDTO)
            .serviceName(headers.get(CommonConstants.SERVICE_NAME))
            .xRequestId(headers.get(CommonConstants.X_REQUEST_ID))
            .build();

    producerService.sendTrackingMessage(kafkaMessageMetadata);
  }

  private TrackingRequestDTO buildTrackingRequest(
      String message,
      Map<String, String> headers,
      String responseStatus,
      String... validationErrors) {

    message =
        SensitiveProcessor.removeFields(
            objectMapper.convertValue(message, new TypeReference<>() {}));
    TrackingRequestDTO.Request request =
        new TrackingRequestDTO.Request(
            CommonConstants.KAFKA_TRACKING_METHOD,
            headers.get(KafkaHeaders.TOPIC),
            buildHeaders(headers),
            message);
    TrackingRequestDTO.Response response =
        new TrackingRequestDTO.Response(
            responseStatus, StringUtils.arrayToCommaDelimitedString(validationErrors));
    TrackingRequestDTO.MetadataTracking metadataTracking =
        new TrackingRequestDTO.MetadataTracking(RequestType.KAFKA, request, response);

    return new TrackingRequestDTO(headers.get(CommonConstants.X_REQUEST_ID), metadataTracking);
  }

  private List<TrackingRequestDTO.Request.Header> buildHeaders(Map<String, String> headers) {
    return headers.keySet().stream()
        .map(key -> new TrackingRequestDTO.Request.Header(key, headers.get(key)))
        .toList();
  }

  private void execute(String message, T data, Map<String, String> headers) {
    try {
      doExecute(data);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      String responseStatus =
          (e instanceof ApplicationException)
              ? ((ApplicationException) e).getCode()
              : e.getMessage();
      sendTrackingTopic(message, headers, responseStatus);
      throwNonRetryableIfCatchApplicationException(e);
    }
  }

  public abstract void doExecute(T data);

  @SneakyThrows
  private void throwNonRetryableIfCatchApplicationException(Exception e) {
    if (!(e instanceof ApplicationException)) {
      throw e;
    }

    throw new NonRetryableException();
  }
}
