package io.conduit.sdk.specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.conduit.grpc.Specifier;
import io.conduit.grpc.SpecifierPluginGrpc;
import io.conduit.sdk.Configurable;
import io.conduit.sdk.Destination;
import io.conduit.sdk.Source;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.enterprise.inject.Instance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

import static java.util.Collections.emptyMap;

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

        Class cfgClass = instance.get().configClass();
        if (cfgClass == null) {
            log.info("no config class");
            return emptyMap();
        }

        // todo take name from @JsonProperty
        return Arrays.stream(FieldUtils.getAllFields(cfgClass))
            .collect(Collectors.toMap(
                Field::getName,
                this::extractParams
            ));
    }

    @SneakyThrows
    private Specifier.Parameter extractParams(Field f) {
        Specifier.Parameter.Builder paramBuilder = Specifier.Parameter.newBuilder()
            .setType(getType(f));

        String def = getDefaultValue(f);
        if (def != null) {
            paramBuilder.setDefault(def);
        }

        for (Annotation annotation : f.getAnnotations()) {
            switch (annotation) {
                case Regex p -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REGEX)
                        .setValue(p.value())
                        .build()
                );
                case Required ignored -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REQUIRED)
                        .build()
                );
                case GreaterThan gt -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_GREATER_THAN)
                        .setValue(String.valueOf(gt.value()))
                        .build()
                );

                case LessThan lt -> paramBuilder.addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_LESS_THAN)
                        .setValue(String.valueOf(lt.value()))
                        .build()
                );
                default -> {
                }
            }
        }

        return paramBuilder.build();
    }

    private String getDefaultValue(Field f) {
        Default def = f.getAnnotation(Default.class);
        return def == null ? null : def.value();
    }

    private Specifier.Parameter.Type getType(Field f) {
        if (String.class.equals(f.getType())) {
            return Specifier.Parameter.Type.TYPE_STRING;
        }
        if (INT_TYPES.contains(f.getType())) {
            return Specifier.Parameter.Type.TYPE_INT;
        }

        throw new IllegalArgumentException("boom");
    }

    private Specification specification() {
        if (specification.isUnsatisfied()) {
            throw new IllegalArgumentException("specification not implemented");
        }

        return specification.get();
    }
}
