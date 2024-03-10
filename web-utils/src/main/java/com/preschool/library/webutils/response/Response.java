package com.preschool.library.webutils.response;

public record Response<T>(Status status, T payload, Object error, Meta meta) {
    private static final String SUCCESS = "SUCCESS";

    public static <T> Response<T> success(T payload) {
        return new Response<>(new Status(SUCCESS), payload, null, null);
    }

    public static <T> Response<T> success(T payload, Meta meta) {
        // TODO: if meta == null then build meta from request-id header
        return new Response<>(new Status(SUCCESS), payload, null, meta);
    }

    public static <T> Response<T> success(String code, T payload, Meta meta) {
        return new Response<>(new Status(code), payload, null, meta);
    }

    public static <T> Response<T> error(String code, String message) {
        return new Response<>(new Status(code, message), null, null, null);
    }

    public static <T> Response<T> error(String code, String message, Object error) {
        return new Response<>(new Status(code, message), null, error, null);
    }

    public static <T> Response<T> error(String code, String message, Object error, Meta meta) {
        // TODO: if meta == null then build meta from request-id header
        return new Response<>(new Status(code, message), null, error, meta);
    }
}
