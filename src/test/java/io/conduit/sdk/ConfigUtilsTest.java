package io.conduit.sdk;

import java.util.Map;

import io.conduit.sdk.specification.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigUtilsTest {
    @Test
    void testWithDefaults_FieldPresent() {
        assertEquals(
            Map.of("fieldFoo", "def"),
            ConfigUtils.withDefaults(TestCfg.class, Map.of("fieldFoo", "def"))
        );
    }

    @Test
    void testWithDefaults_FieldNotPresent() {
        assertEquals(
            Map.of("fieldFoo", "abc"),
            ConfigUtils.withDefaults(TestCfg.class, Map.of())
        );
    }

    @Test
    void testParse() {
        assertEquals(
            new TestCfg("abc", 11),
            ConfigUtils.parse(Map.of("fieldBar", "11"), TestCfg.class)
        );
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestCfg {
        @Default("abc")
        private String fieldFoo;

        private int fieldBar;
    }
}