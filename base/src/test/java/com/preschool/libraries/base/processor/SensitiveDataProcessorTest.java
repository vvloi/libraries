package com.preschool.libraries.base.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SensitiveDataProcessorTest {
  @BeforeEach
  public void setup() {
    SensitiveContext.SensitiveConfig sensitiveConfig =
        SensitiveContext.SensitiveConfig.builder()
            .fields(List.of("password"))
            .hideCharacters(5)
            .sensitiveHideType(SensitiveHideType.PARTIAL)
            .build();

    SensitiveContext.setContext(sensitiveConfig);
  }

  @Test
  public void shouldHideSensitiveData_stringValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", "username");
    map.put("password", "password123");

    SensitiveProcessor.hideSensitiveFields(map);

    assertEquals("*****ord123", map.get("password"));
  }

  @Test
  public void shouldHideSensitiveData_primitiveValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", "username");
    map.put("password", 123456789);

    SensitiveProcessor.hideSensitiveFields(map);

    assertEquals("*****6789", map.get("password"));
  }

  @Test
  public void shouldHideSensitiveData_primitiveWrapperValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", "username");
    map.put("password", 987654321L);

    SensitiveProcessor.hideSensitiveFields(map);

    assertEquals("*****4321", map.get("password"));
  }

  @Test
  public void shouldRemoveSensitiveData_primitiveWrapperValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("username", "username");
    map.put("password", 987654321L);

    SensitiveProcessor.removeFields(map);

    assertNull(map.get("password"));
  }
}
