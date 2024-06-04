package io.conduit.sdk;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.conduit.sdk.specification.Default;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ConfigUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigUtils() {
    }

    public static <T> T parse(Map<String, String> map, Class<T> clazz) {
        return mapper.convertValue(withDefaults(clazz, map), clazz);
    }

    public static Map<String, String> withDefaults(Class cfgClass, Map<String, String> cfgMap) {
        Map<String, String> map = new HashMap<>();
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
