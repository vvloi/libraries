package com.preschool.libraries.base.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.libraries.base.common.AppObjectMapper;
import org.junit.jupiter.api.Test;

public class MaskSensitiveDataTest {
    @Test
    public void demo() throws Exception {
        ObjectMapper mapper = new AppObjectMapper();

        SensitiveDataTest obj = new SensitiveDataTest("username", "123456789");
        String json = mapper.writeValueAsString(obj);

        String expectedJson = "{\"username\":\"username\",\"password\":\"*****6789\"}";
        assertEquals(expectedJson, json);
    }

    public record SensitiveDataTest(String username, @Sensitive String password) {}
}
