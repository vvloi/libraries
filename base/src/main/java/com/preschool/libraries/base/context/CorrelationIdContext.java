package com.preschool.libraries.base.context;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CorrelationIdContext {
  private static final ThreadLocal<CorrelationIdData> context = new ThreadLocal<>();

  public static void setContext(String xRequestId) {
    context.set(new CorrelationIdData(xRequestId));
  }

  public static String getRequestId() {
    return context.get() == null ? UUID.randomUUID().toString() : context.get().xRequestId;
  }

  public static void clearContext() {
      context.remove();
  }

  @Builder
  public record CorrelationIdData(String xRequestId) {}
}
