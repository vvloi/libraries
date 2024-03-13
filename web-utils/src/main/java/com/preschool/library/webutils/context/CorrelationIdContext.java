package com.preschool.library.webutils.context;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CorrelationIdContext {
    private static final ThreadLocal<CorrelationIdData> context = new ThreadLocal<>();

    public static void setContext(String xRequestId) {
        context.set(new CorrelationIdData(xRequestId));
    }

    public static String getRequestId() {
        return context.get() == null ? "" : context.get().xRequestId;
    }

    @Builder
    public record CorrelationIdData(String xRequestId) {}
}
