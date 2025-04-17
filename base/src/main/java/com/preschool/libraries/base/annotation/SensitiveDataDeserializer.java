package com.preschool.libraries.base.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.preschool.libraries.base.enumeration.MaskType;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

// Custom deserializer for fields annotated with @SensitiveData
public class SensitiveDataDeserializer extends JsonDeserializer<String> {

    private static final String MASKED_VALUE = "****";
    private static final Map<MaskType, Function<String, String>> MASK_STRATEGIES = Map.of(
            MaskType.FULL, value -> MASKED_VALUE,
            MaskType.PARTIAL, value -> partialMask(value, 4) // Default prefix length, overridden by annotation
    );

    private final SensitiveData annotation;

    public SensitiveDataDeserializer(SensitiveData annotation) {
        this.annotation = annotation;
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        // Fail-fast: Return null if input is null
        String value = parser.getText();
        if (value == null) {
            return null;
        }

        // Apply masking strategy based on annotation
        Function<String, String> maskStrategy = MASK_STRATEGIES.getOrDefault(annotation.maskType(), v -> v);
        return annotation.maskType() == MaskType.PARTIAL
                ? partialMask(value, annotation.visiblePrefixLength())
                : maskStrategy.apply(value);
    }

    // Helper method for partial masking
    private static String partialMask(String value, int visibleLength) {
        // Fail-fast: Return masked value if input is too short
        if (value.isEmpty()) {
            return MASKED_VALUE;
        }

        int prefixLength = Math.min(visibleLength, value.length());
        return value.substring(0, prefixLength) + MASKED_VALUE;
    }
}