package com.preschool.libraries.base.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.preschool.libraries.base.common.AppObjectMapper;
import com.preschool.libraries.base.common.CommonConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;

public class SensitiveProcessor {
  private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;

  static {
    WRAPPER_TYPE_MAP = new HashMap<>(16);
    WRAPPER_TYPE_MAP.put(Integer.class, int.class);
    WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
    WRAPPER_TYPE_MAP.put(Character.class, char.class);
    WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
    WRAPPER_TYPE_MAP.put(Double.class, double.class);
    WRAPPER_TYPE_MAP.put(Float.class, float.class);
    WRAPPER_TYPE_MAP.put(Long.class, long.class);
    WRAPPER_TYPE_MAP.put(Short.class, short.class);
    WRAPPER_TYPE_MAP.put(Void.class, void.class);
    WRAPPER_TYPE_MAP.put(String.class, String.class);
  }

  public static void hideSensitiveFields(Map<String, Object> data) {
    SensitiveContext.SensitiveConfig sensitiveConfig = SensitiveContext.getContext();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      if (!WRAPPER_TYPE_MAP.containsKey(entry.getValue().getClass())) {
        hideSensitiveFields(
            new AppObjectMapper().convertValue(entry.getValue(), new TypeReference<>() {}));
        continue;
      }

      String key = entry.getKey();
      if (sensitiveConfig.fields().contains(key)) {
        data.put(key, maskingSensitiveData(data.get(key), sensitiveConfig));
      }
    }
  }

  private static Object maskingSensitiveData(
      Object value, SensitiveContext.SensitiveConfig sensitiveConfig) {
    return Optional.ofNullable(value)
        .map(String::valueOf)
        .filter(
            v ->
                Objects.requireNonNullElse(
                        sensitiveConfig.sensitiveHideType(),
                        CommonConstants.SENSITIVE_HIDE_TYPE_DEFAULT)
                    == SensitiveHideType.FULLY)
        .map(vStr -> "*".repeat(vStr.length()))
        .orElseGet(
            () -> {
              String vStr = String.valueOf(value);
              int hideChar = Math.min(sensitiveConfig.hideCharacters(), vStr.length());
              return "*".repeat(hideChar) + vStr.substring(hideChar);
            });
  }

  @SneakyThrows
  public static String removeFields(Map<String, Object> data) {
    executeRemove(data);
    return new AppObjectMapper().writeValueAsString(data);
  }

  private static void executeRemove(Map<String, Object> data) {
    Map<String, Object> clone = new HashMap<>(data);
    SensitiveContext.SensitiveConfig sensitiveConfig = SensitiveContext.getContext();
    for (Map.Entry<String, Object> entry : clone.entrySet()) {
      if (!WRAPPER_TYPE_MAP.containsKey(entry.getValue().getClass())) {
        executeRemove(
            new AppObjectMapper().convertValue(entry.getValue(), new TypeReference<>() {}));
        continue;
      }

      String key = entry.getKey();
      if (Objects.requireNonNullElse(
              sensitiveConfig.removeFields(), CommonConstants.REMOVE_HIDE_FIELDS_DEFAULT)
          .contains(key)) {
        data.remove(key);
      }
    }
  }
}
