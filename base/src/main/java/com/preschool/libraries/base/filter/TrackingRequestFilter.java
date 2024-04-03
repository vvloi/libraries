package com.preschool.libraries.base.filter;

import com.preschool.libraries.base.annotation.SensitiveProcessor;
import com.preschool.libraries.base.common.AppObjectMapper;
import com.preschool.libraries.base.context.CorrelationIdContext;
import com.preschool.libraries.base.dto.TrackingRequestDTO;
import com.preschool.libraries.base.eumeration.RequestType;
import com.preschool.libraries.base.filter.requestcache.PayloadCachingRequest;
import com.preschool.libraries.base.kafka.KafkaMessageMetadata;
import com.preschool.libraries.base.kafka.ProducerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingRequestFilter extends OncePerRequestFilter {

    private final ProducerService producerService;
    private final AppObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        PayloadCachingRequest cachingRequest = new PayloadCachingRequest(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        TrackingRequestDTO.Request requestTrackingData = exploreRequest(cachingRequest);
        filterChain.doFilter(cachingRequest, cachingResponse);
        TrackingRequestDTO.Response responseTrackingData = exploreResponse(cachingResponse);

        sendTrackingMessage(requestTrackingData, responseTrackingData);
    }

    @SneakyThrows
    private TrackingRequestDTO.Request exploreRequest(PayloadCachingRequest cachingRequest) {
        String method = cachingRequest.getMethod();
        String url = cachingRequest.getRequestURI();
        List<TrackingRequestDTO.Request.Header> headers = getHeaders(cachingRequest);
        String payload = new String(cachingRequest.getCachedPayload());
        log.info("{} {}", method, url);
        log.debug("Headers: [{}]", objectMapper.writeValueAsString(headers));

        Object o = objectMapper.readValue(payload, Object.class);
        SensitiveProcessor.sensitiveData(o);
        log.debug("Payload: [{}]", objectMapper.writeValueAsString(o));

        return new TrackingRequestDTO.Request(method, url, headers, payload);
    }

    private List<TrackingRequestDTO.Request.Header> getHeaders(
            PayloadCachingRequest cachingRequestWrapper) {
        return Optional.ofNullable(cachingRequestWrapper.getHeaderNames())
                .filter(Enumeration::hasMoreElements)
                .map(Enumeration::nextElement)
                .map(
                        headerName ->
                                new TrackingRequestDTO.Request.Header(
                                        headerName, cachingRequestWrapper.getHeader(headerName)))
                .stream()
                .toList();
    }

    @SneakyThrows
    private TrackingRequestDTO.Response exploreResponse(
            ContentCachingResponseWrapper responseWrapper) {
        String body = new String(responseWrapper.getContentAsByteArray());
        Object o = objectMapper.readValue(body, Object.class);
        SensitiveProcessor.sensitiveData(o);
        log.debug("Response: [{}]", objectMapper.writeValueAsString(o));
        responseWrapper.copyBodyToResponse();
        return new TrackingRequestDTO.Response(
                HttpStatus.valueOf(responseWrapper.getStatus()).name(), body);
    }

    private void sendTrackingMessage(
            TrackingRequestDTO.Request request, TrackingRequestDTO.Response response) {
        String requestId = CorrelationIdContext.getRequestId();

        TrackingRequestDTO.MetadataTracking metadataTracking =
                new TrackingRequestDTO.MetadataTracking(RequestType.REST_API, request, response);
        TrackingRequestDTO trackingRequestDTO = new TrackingRequestDTO(requestId, metadataTracking);

        KafkaMessageMetadata<TrackingRequestDTO, Void> kafkaMessageMetadata =
                KafkaMessageMetadata.<TrackingRequestDTO, Void>builder()
                        .data(trackingRequestDTO)
                        .xRequestId(requestId)
                        .build();

        producerService.sendTrackingMessage(kafkaMessageMetadata);
    }
}
