package com.preschool.libraries.base.annotation;

import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SensitiveProcessor {
    public static void sensitiveData(Object o) {
        for (Field f : o.getClass().getDeclaredFields()) {
            if (f.getType().isPrimitive()) {
                if (!f.isAnnotationPresent(Sensitive.class)) {
                    continue;
                }

                executeHideSensitiveData(o, f);
                continue;
            }

            recursionHideSensitiveData(o, f);
        }
    }

    private static void executeHideSensitiveData(Object o, Field f) {
        boolean accessible = f.canAccess(o);
        f.setAccessible(true);
        try {
            f.set(o, "********");
        } catch (IllegalAccessException e) {
            log.error("Can't hide sensitive data for field name [{}]", f.getName());
        } finally {
            f.setAccessible(accessible);
        }
    }

    private static void recursionHideSensitiveData(Object o, Field f) {
        try {
            Object childObject = f.get(o);
            sensitiveData(childObject);
        } catch (IllegalAccessException e) {
            log.error("Can't get value of field name [{}]", f.getName());
        }
    }
}
