package com.preschool.libraries.base.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AppObjectMapper extends ObjectMapper {
  public AppObjectMapper() {
    super();
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    setSerializationInclusion(JsonInclude.Include.NON_NULL);
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    registerModule(new JavaTimeModule());

    setAnnotationIntrospector();
  }

  private void setAnnotationIntrospector() {
    AnnotationIntrospector sis = getSerializationConfig().getAnnotationIntrospector();
    AnnotationIntrospector dis = getDeserializationConfig().getAnnotationIntrospector();

    AnnotationIntrospector is1 =
        AnnotationIntrospectorPair.pair(sis, new MaskingSensitiveDataIntrospector());
    AnnotationIntrospector is2 =
        AnnotationIntrospectorPair.pair(dis, new MaskingSensitiveDataIntrospector());

    setAnnotationIntrospectors(is1, is2);
  }
}
