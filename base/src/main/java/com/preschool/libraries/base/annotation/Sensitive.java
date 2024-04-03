package com.preschool.libraries.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sensitive {
    int hideCharacters() default Integer.MAX_VALUE;

    SensitiveHideType hideType() default SensitiveHideType.PARTIAL;
}
