package io.conduit.sdk;

import java.util.Map;

import io.conduit.sdk.specification.Default;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigUtilsTest {
    @Test
    void testWithDefaults_FieldPresent() {
        assertEquals(
            Map.of("field", "def"),
            ConfigUtils.withDefaults(TestConfig.class, Map.of("field", "def"))
        );
    }

    @Test
    void testWithDefaults_FieldNotPresent() {
        assertEquals(
            Map.of("field", "abc"),
            ConfigUtils.withDefaults(TestConfig.class, Map.of())
        );
    }

    private static class TestConfig {
        @Default("abc")
        private String field;
    }
}