package com.preschool.libraries.base.annotation;

import com.preschool.libraries.base.enumeration.MaskType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveData {
  MaskType maskType() default MaskType.FULL;

  int visiblePrefixLength() default 4;
}
