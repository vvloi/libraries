package com.preschool.library.kafkautils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.library.core.AppObjectMapper;
import com.preschool.library.core.ApplicationConstants;
import com.preschool.library.core.dto.TrackingRequestDTO;
import com.preschool.library.core.eumeration.RequestType;
import com.preschool.library.core.exception.ApplicationException;
import com.preschool.library.core.exception.LibraryErrorCode;
import com.preschool.library.core.exception.NonRetryableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class ConsumerAbstract<T> {
    public void executeHandle(String message, Class<T> tClass, Map<String, String> headers) {
        T data = parseMessage(message, tClass);
        Optional.of(data)
                .map(this::validate)
                .flatMap(
                        validationErrors -> {
                            log.error(
                                    "Having some errors [{}] when validating message data",
                                    String.join(",", validationErrors));
                            buildTrackingRequest(
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
                                    headers.get(ApplicationConstants.X_REQUEST_ID));
                            doExecute(data);
                            return Optional.empty();
                        });
    }

    private T parseMessage(String message, Class<T> tClass) {
        T data = null;
        try {
            ObjectMapper objectMapper = new AppObjectMapper();
            data = objectMapper.readValue(message, tClass);
        } catch (Exception e) {
            log.error("An error occur, NonRetryableException will be thrown. {}", e.getMessage(), e);
            throwRetryableException(e);
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

    private TrackingRequestDTO buildTrackingRequest(
            String message,
            Map<String, String> headers,
            String responseStatus,
            String... validationErrors) {
        TrackingRequestDTO.Request request =
                new TrackingRequestDTO.Request(
                        ApplicationConstants.KAFKA_TRACKING_METHOD,
                        headers.get(KafkaHeaders.TOPIC),
                        buildHeaders(headers),
                        message);
        TrackingRequestDTO.Response response =
                new TrackingRequestDTO.Response(
                        responseStatus, StringUtils.arrayToCommaDelimitedString(validationErrors));
        TrackingRequestDTO.MetadataTracking metadataTracking =
                new TrackingRequestDTO.MetadataTracking(RequestType.KAFKA, request, response);

        return new TrackingRequestDTO(headers.get(ApplicationConstants.X_REQUEST_ID), metadataTracking);
    }

    private List<TrackingRequestDTO.Request.Header> buildHeaders(Map<String, String> headers) {
        return headers.keySet().stream()
                .map(key -> new TrackingRequestDTO.Request.Header(key, headers.get(key)))
                .toList();
    }

    // override it if you want to retry message when can't parse kafka message
    public void throwRetryableException(Exception e) {
        throw new NonRetryableException();
    }

    private void execute(String message, T data, Map<String, String> headers) {
        try {
            doExecute(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throwNonRetryableIfCatchApplicationException(message, headers, e);

            buildTrackingRequest(message, headers, e.getMessage());
            throw e;
        }
    }

    public abstract void doExecute(T data);

    private void throwNonRetryableIfCatchApplicationException(
            String message, Map<String, String> headers, Exception e) {
        if (!(e instanceof ApplicationException)) {
            return;
        }

        String responseStatus = ((ApplicationException) e).getCode();
        buildTrackingRequest(message, headers, responseStatus);

        throw new NonRetryableException();
    }
}
