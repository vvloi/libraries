package com.preschool.libraries.base.common;

import com.preschool.libraries.base.annotation.SensitiveHideType;
import java.util.List;

public class CommonConstants {
  public static final String SUCCESS = "SUCCESS";
  public static final String SERVICE_NAME = "Service-Name";
  public static final String X_REQUEST_ID = "X-Request-Id";
  public static final String KAFKA_TRACKING_METHOD = "Listener";
  public static final String COLLECT_METRICS = "COLLECT_METRICS";
  public static final SensitiveHideType SENSITIVE_HIDE_TYPE_DEFAULT = SensitiveHideType.FULLY;
  public static final List<String> REMOVE_HIDE_FIELDS_DEFAULT = List.of("password", "pwd");
}
