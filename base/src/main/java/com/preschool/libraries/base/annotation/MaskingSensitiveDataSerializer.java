package com.preschool.libraries.base.annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.preschool.libraries.base.context.SensitiveContext;
import java.io.IOException;

public class MaskingSensitiveDataSerializer extends StdSerializer<String> {
    public MaskingSensitiveDataSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        SensitiveContext.SensitiveConfig sensitiveConfig = SensitiveContext.getContext();

        if (SensitiveHideType.FULLY == sensitiveConfig.sensitiveHideType()) {
            hideFully(value, gen);
        } else {
            hidePartial(sensitiveConfig.hideCharacters(), value, gen);
        }

        SensitiveContext.clearContext();
    }

    private void hideFully(String value, JsonGenerator gen) throws IOException {
        gen.writeString("*".repeat(value.length()));
    }

    private void hidePartial(int hideCharacters, String value, JsonGenerator gen) throws IOException {
        if (hideCharacters == Integer.MAX_VALUE) {
            hideCharacters = 5;
        }

        if (hideCharacters > value.length()) {
            hideCharacters = value.length();
        }

        gen.writeString("*".repeat(hideCharacters).concat(value.substring(hideCharacters)));
    }
}
