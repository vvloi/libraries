package com.preschool.library.webutils.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.library.core.ApplicationConstants;
import com.preschool.library.core.dto.TrackingRequestDTO;
import com.preschool.library.core.eumeration.RequestType;
import com.preschool.library.kafkautils.KafkaMessageMetadata;
import com.preschool.library.kafkautils.ProducerService;
import com.preschool.library.webutils.context.CorrelationIdContext;
import com.preschool.library.webutils.filter.requestcache.PayloadCachingRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class TrackingRequestFilter extends OncePerRequestFilter {
    @Value("application.default-kafka.metrics-topic")
    private String metricsTopic;

    private final ProducerService producerService;

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
        log.debug("Headers: [{}]", new ObjectMapper().writeValueAsString(headers));
        log.debug("Payload: [{}]", payload);

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
        log.debug("Response: [{}]", body);
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
                        .topic(metricsTopic)
                        .event(ApplicationConstants.COLLECT_METRICS)
                        .data(trackingRequestDTO)
                        .xRequestId(requestId)
                        .build();

        producerService.sendMessage(kafkaMessageMetadata);
    }
}
