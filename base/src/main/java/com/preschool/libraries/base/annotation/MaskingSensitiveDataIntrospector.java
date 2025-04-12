package com.preschool.libraries.base.annotation;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.preschool.libraries.base.context.SensitiveContext;

public class MaskingSensitiveDataIntrospector extends NopAnnotationIntrospector {
  @Override
  public Object findSerializer(Annotated am) {
    Sensitive annotation = am.getAnnotation(Sensitive.class);
    if (annotation != null) {
      SensitiveContext.setContext(
          SensitiveContext.SensitiveConfig.builder()
              .hideCharacters(annotation.hideCharacters())
              .sensitiveHideType(annotation.hideType())
              .build());
      return MaskingSensitiveDataSerializer.class;
    }

    return null;
  }
}
