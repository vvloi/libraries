package com.preschool.libraries.base.context;

import com.preschool.libraries.base.annotation.SensitiveHideType;
import java.util.List;
import lombok.Builder;

public class SensitiveContext {
  private static final ThreadLocal<SensitiveContext.SensitiveConfig> context = new ThreadLocal<>();

  public static void setContext(SensitiveConfig sensitiveConfig) {
    context.set(sensitiveConfig);
  }

  public static SensitiveConfig getContext() {
    return context.get();
  }

  public static void clearContext() {
    context.remove();
  }

  @Builder
  public record SensitiveConfig(
      int hideCharacters,
      SensitiveHideType sensitiveHideType,
      List<String> fields,
      List<String> removeFields) {}
}
