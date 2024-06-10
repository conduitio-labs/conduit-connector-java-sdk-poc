package io.conduit.sdk.specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import io.conduit.grpc.Specifier;
import io.conduit.grpc.SpecifierPluginGrpc;
import io.conduit.sdk.Configurable;
import io.conduit.sdk.Destination;
import io.conduit.sdk.Source;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.enterprise.inject.Instance;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

@GrpcService
@Slf4j
public class SpecService extends SpecifierPluginGrpc.SpecifierPluginImplBase {
    private static final Set<Class> INT_TYPES = Set.of(
        Integer.class,
        int.class,
        Long.class,
        long.class,
        Byte.class,
        byte.class
        // todo add BigXYZ, AtomicXYZ
    );
    private Instance<Specification> specification;
    private Instance<Destination> destination;
    private Instance<Source> source;

    public SpecService(Instance<Specification> specification, Instance<Source> source, Instance<Destination> destination) {
        if (destination == null && source == null) {
            throw new IllegalArgumentException("source and destination are both null");
        }

        this.specification = specification;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void specify(Specifier.Specify.Request request,
                        StreamObserver<Specifier.Specify.Response> responseObserver) {

        log.info("specify called");

        log.info("extracting source params");
        Map<String, Specifier.Parameter> srcParams = extractParams(source);

        log.info("extracting destination params");
        Map<String, Specifier.Parameter> dstParams = extractParams(destination);

        responseObserver.onNext(
            Specifier.Specify.Response.newBuilder()
                .setName(specification().name())
                .setSummary(specification().summary())
                .setDescription(specification().description())
                .setVersion(specification().version())
                .setAuthor(specification().author())
                .putAllSourceParams(srcParams)
                .putAllDestinationParams(dstParams)
                .build()
        );
        responseObserver.onCompleted();
    }

    private Map<String, Specifier.Parameter> extractParams(Instance<? extends Configurable> instance) {
        if (instance == null || instance.isUnsatisfied()) {
            log.info("no source/destination instance");
            return emptyMap();
        }

        return extractParamsFromClass("", instance.get().configClass());
    }

    private Map<String, Specifier.Parameter> extractParamsFromClass(String prefix, Class cfgClass) {
        if (cfgClass == null) {
            log.info("no config class");
            return emptyMap();
        }

        // todo take name from @JsonProperty
        return Arrays.stream(FieldUtils.getAllFields(cfgClass))
            .map(this::extractParamsFromField)
            .flatMap(m -> m.entrySet().stream())
            .collect(toMap(
                entry -> withPrefix(prefix, entry),
                Map.Entry::getValue
            ));
    }

    private static String withPrefix(String prefix, Map.Entry<String, Specifier.Parameter> entry) {
        if ("".equals(prefix)) {
            return entry.getKey();
        }

        return prefix + "." + entry.getKey();
    }

    @SneakyThrows
    private Map<String, Specifier.Parameter> extractParamsFromField(Field f) {
        if (!f.getType().getPackageName().startsWith("java.lang")) {
            return extractParamsFromClass(f.getName(), f.getType());
        }

        Specifier.Parameter.Builder paramBuilder = Specifier.Parameter.newBuilder()
            .setType(getParameterType(f));

        String def = getDefaultValue(f);
        if (def != null) {
            paramBuilder.setDefault(def);
        }

        for (Annotation annotation : f.getAnnotations()) {
            switch (annotation) {
                case Pattern p -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REGEX)
                        .setValue(p.regexp())
                        .build()
                );
                case Required ignored -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REQUIRED)
                        .build()
                );
                case Min min -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_GREATER_THAN)
                        .setValue(String.valueOf(min.value()))
                        .build()
                );

                case Max max -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_LESS_THAN)
                        .setValue(String.valueOf(max.value()))
                        .build()
                );
                default -> {
                }
            }
        }

        return Map.of(f.getName(), paramBuilder.build());
    }

    private String getDefaultValue(Field f) {
        Default def = f.getAnnotation(Default.class);
        return def == null ? null : def.value();
    }

    private Specifier.Parameter.Type getParameterType(Field f) {
        if (String.class.equals(f.getType())) {
            return Specifier.Parameter.Type.TYPE_STRING;
        }
        if (INT_TYPES.contains(f.getType())) {
            return Specifier.Parameter.Type.TYPE_INT;
        }

        throw new IllegalArgumentException("unsupported field type: " + f.getType().getName());
    }

    private Specification specification() {
        if (specification.isUnsatisfied()) {
            throw new IllegalArgumentException("specification not implemented");
        }

        return specification.get();
    }
}
