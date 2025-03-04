package com.preschool.libraries.base.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.libraries.base.processor.SensitiveProcessor;
import feign.InvocationContext;
import feign.Request;
import feign.Response;
import feign.ResponseInterceptor;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingInterceptor implements ResponseInterceptor {
    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        try (Response response = invocationContext.response();
                Response cloneResponse =
                        response.toBuilder().body(new CachedBody(response.body())).build()) {
            // test ci
            if (response.body() == null) {
                return chain.next(invocationContext);
            }
            Request request = response.request();
            log.info("{} {}", request.httpMethod(), request.url());
            logHeaders("request", request.headers());
            logPayload("request", request.body());

            log.info("response status: {}", response.status());
            logHeaders("response", request.headers());
            logPayload("response", response.body().asInputStream().readAllBytes());
            return invocationContext.decoder().decode(cloneResponse, invocationContext.returnType());
        }
    }

    private void logHeaders(String prefix, java.util.Map<String, Collection<String>> headers) {
        String headersLog =
                headers.entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + String.join(",", entry.getValue()))
                        .collect(Collectors.joining("; "));
        log.info("{} headers: {}", prefix, headersLog);
    }

    private void logPayload(String prefix, byte[] body) {
        if (body != null && body.length > 0) {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            Map<String, Object> payload =
                    new ObjectMapper().convertValue(bodyString, new TypeReference<>() {});
            log.debug("{} payload: {}", prefix, SensitiveProcessor.removeFields(payload));
        }
    }
}
