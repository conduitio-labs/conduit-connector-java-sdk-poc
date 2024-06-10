package io.conduit.sdk;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import io.conduit.sdk.specification.Default;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

import static java.util.stream.Collectors.toMap;

public class ConfigUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigUtils() {
    }

    public static <T> T parse(Map<String, String> map, Class<T> clazz) {
        return mapper.convertValue(validate(clazz, map), clazz);
    }

    @SneakyThrows
    public static <T> Map<String, Object> validate(Class<T> cfgClass, Map<String, String> cfgMap) {
        if (cfgClass == null) {
            return cfgMap.entrySet()
                .stream()
                .collect(toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
        }

        var withDefaults = withDefaults(cfgClass, JsonUnflattener.unflattenAsMap(cfgMap));

        // Convert to POJO and validate
        T cfg = mapper.convertValue(withDefaults, cfgClass);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<T>> violations = factory.getValidator().validate(cfg);
            if (violations != null && !violations.isEmpty()) {
                throw new IllegalArgumentException("validation failed: " + violations);
            }
        }

        // Flatten again, as a Conduit connector config is always a map of strings to strings
        return withDefaults;
    }

    @SneakyThrows
    public static <T> Map<String, String> validateAndFlatten(Class<T> cfgClass, Map<String, String> cfgMap) {
        var validated = validate(cfgClass, cfgMap);


        Map<String, Object> flattenedObj = JsonFlattener.flattenAsMap(mapper.writeValueAsString(validated));
        Map<String, String> flattened = new HashMap<>();
        for (Map.Entry<String, Object> e : flattenedObj.entrySet()) {
            flattened.put(e.getKey(), String.valueOf(e.getValue()));
        }

        return flattened;
    }

    private static Map<String, Object> withDefaults(Class cfgClass, Map<String, Object> cfgMap) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : FieldUtils.getFieldsWithAnnotation(cfgClass, Default.class)) {
            map.put(
                field.getName(),
                field.getAnnotation(Default.class).value()
            );
        }

        map.putAll(cfgMap);

        return map;
    }
}
