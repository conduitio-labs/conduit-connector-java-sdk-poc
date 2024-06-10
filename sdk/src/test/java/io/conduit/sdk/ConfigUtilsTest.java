package io.conduit.sdk;

import java.util.Map;

import io.conduit.sdk.specification.Default;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigUtilsTest {
    @Test
    void testWithDefaults_FieldPresent() {
        assertEquals(
            Map.of("fieldFoo", "def", "fieldBar", "10"),
            ConfigUtils.validate(TestCfg.class, Map.of("fieldFoo", "def", "fieldBar", "10"))
        );
    }

    @Test
    void testWithDefaults_FieldNotPresent() {
        assertEquals(
            Map.of("fieldFoo", "abc", "fieldBar", "10"),
            ConfigUtils.validate(TestCfg.class, Map.of("fieldBar", "10"))
        );
    }

    @Test
    void testParse() {
        assertEquals(
            new TestCfg("abc", 11, null),
            ConfigUtils.parse(Map.of("fieldBar", "11"), TestCfg.class)
        );
    }

    @Test
    void testParseNestedJSON() {
        assertEquals(
            new TestCfg("abc", 11, new TestNestedCfg("foobar")),
            ConfigUtils.parse(Map.of("fieldBar", "11", "nestedCfg.stringField", "foobar"), TestCfg.class)
        );
    }

    @Test
    void testFailedValidation() {
        var e = assertThrows(
            IllegalArgumentException.class,
            () -> ConfigUtils.parse(Map.of("fieldBar", "21"), TestCfg.class)
        );
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestCfg {
        @Default("abc")
        private String fieldFoo;

        @Min(2)
        @Max(20)
        private int fieldBar;

        @Valid
        private TestNestedCfg nestedCfg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestNestedCfg {
        private String stringField;
    }
}
