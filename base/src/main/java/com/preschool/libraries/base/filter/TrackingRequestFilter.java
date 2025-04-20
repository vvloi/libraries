package com.preschool.libraries.base.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.libraries.base.annotation.SensitiveDataModule;
import com.preschool.libraries.base.kafka.ProducerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class TrackingRequestFilter extends OncePerRequestFilter {
  private final ObjectMapper loggingMapper;
  private final ProducerService producerService;

  public TrackingRequestFilter(ProducerService producerService) {
    // Initialize ObjectMapper with custom module for logging
    this.loggingMapper = new ObjectMapper();
    this.loggingMapper.registerModule(new SensitiveDataModule());
    this.producerService = producerService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    chain.doFilter(wrappedRequest, wrappedResponse);

    logRequest(wrappedRequest);
    logResponse(wrappedResponse);

    wrappedResponse.copyBodyToResponse();
    //    producerService.sendTrackingMessage();
  }

  private void logRequest(ContentCachingRequestWrapper request) {
    try {
      // Fail-fast: Skip if content is empty or not JSON
      String contentType = request.getContentType();
      String content = new String(request.getContentAsByteArray(), request.getCharacterEncoding());
      if (content.isEmpty() || contentType == null || !contentType.contains("application/json")) {
        log.info(
            "Request: {} {}, Body=Non-JSON or empty", request.getMethod(), request.getRequestURI());
        return;
      }

      // Parse and log with masked data
      Object requestObject = loggingMapper.readValue(content, Object.class);
      log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
      log.info("Body: {}", loggingMapper.writeValueAsString(requestObject));
    } catch (Exception e) {
      log.error("Failed to log request", e);
    }
  }

  private void logResponse(ContentCachingResponseWrapper response) {
    try {
      // Fail-fast: Skip if content is empty or not JSON
      String contentType = response.getContentType();
      String content =
          new String(response.getContentAsByteArray(), response.getCharacterEncoding());
      if (content.isEmpty() || contentType == null || !contentType.contains("application/json")) {
        log.info("Response: Status={}, Body=Non-JSON or empty", response.getStatus());
        return;
      }

      // Parse and log with masked data
      Object responseObject = loggingMapper.readValue(content, Object.class);
      log.info("Response: Status={}", response.getStatus());
      log.info("Response body: {}", loggingMapper.writeValueAsString(responseObject));
    } catch (Exception e) {
      log.error("Failed to log response", e);
    }
  }
}
