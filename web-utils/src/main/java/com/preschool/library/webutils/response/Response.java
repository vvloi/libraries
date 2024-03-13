package com.preschool.library.webutils.response;

import com.preschool.library.webutils.context.CorrelationIdContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public record Response<T>(Status status, T payload, Object error, Meta meta) {
    private static final String SUCCESS = "SUCCESS";

    public static <T> Response<T> success(T payload) {
        return new Response<>(new Status(SUCCESS), payload, null, buildMeta());
    }

    public static <T> Response<T> success(T payload, Meta meta) {
        // TODO: if meta == null then build meta from request-id header
        return new Response<>(new Status(SUCCESS), payload, null, meta);
    }

    public static <T> Response<T> success(String code, T payload, Meta meta) {
        return new Response<>(new Status(code), payload, null, meta != null ? meta : buildMeta());
    }

    public static <T> Response<T> error(String code, String message) {

        return new Response<>(new Status(code, message), null, null, buildMeta());
    }

    public static <T> Response<T> error(HttpStatus status) {

        return new Response<>(
                new Status(status.name(), status.getReasonPhrase()), null, null, buildMeta());
    }

    public static <T> Response<T> error(String code, String message, Object error) {
        return new Response<>(new Status(code, message), null, error, buildMeta());
    }

    public static <T> Response<T> error(String code, String message, Object error, Meta meta) {
        // TODO: if meta == null then build meta from request-id header
        return new Response<>(
                new Status(code, message), null, error, meta != null ? null : buildMeta());
    }

    private static Meta buildMeta() {
        return Optional.ofNullable(CorrelationIdContext.getRequestId())
                .map(Meta::new)
                .orElse(new Meta(UUID.randomUUID().toString()));
    }
}
