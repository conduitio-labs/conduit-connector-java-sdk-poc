package io.conduit.sdk;

import io.conduit.grpc.Specifier;
import io.conduit.sdk.specification.Default;
import io.conduit.sdk.specification.Required;
import io.conduit.sdk.specification.SpecService;
import io.conduit.sdk.specification.Specification;
import io.grpc.stub.StreamObserver;
import jakarta.enterprise.inject.Instance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecServiceTest {
    SpecService underTest;

    @Mock
    Specification specification;
    @Mock
    Source source;
    @Mock
    Destination destination;
    @Mock
    StreamObserver<Specifier.Specify.Response> responseObs;

    @BeforeEach
    void setUp() {
        prepareSpecificationMock();
        underTest = new SpecService(
            makeInstanceMock(specification),
            makeInstanceMock(source),
            makeInstanceMock(destination)
        );
    }

    private void prepareSpecificationMock() {
        when(specification.name()).thenReturn("test-name");
        when(specification.summary()).thenReturn("test summary");
        when(specification.description()).thenReturn("test description");
        when(specification.author()).thenReturn("Test Company, Inc.");
        when(specification.version()).thenReturn("v0.20.30");
    }

    private Instance makeInstanceMock(Object obj) {
        var mock = Mockito.mock(Instance.class);
        when(mock.get()).thenReturn(obj);
        when(mock.isUnsatisfied()).thenReturn(obj == null);

        return mock;
    }

    @Test
    void testSourceParams() {
        when(source.configClass()).thenReturn(SourceConfig.class);

        underTest.specify(Specifier.Specify.Request.newBuilder().build(), responseObs);

        var captor = ArgumentCaptor.forClass(Specifier.Specify.Response.class);
        verify(responseObs).onNext(captor.capture());

        var sourceParams = captor.getValue().getSourceParamsMap();
        assertNotNull(sourceParams);
        // @Regex, @Required
        assertEquals(
            Specifier.Parameter.newBuilder()
                .setType(Specifier.Parameter.Type.TYPE_STRING)
                .addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REGEX)
                        .setValue("abc.*def")
                        .build()
                )
                .addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_REQUIRED)
                        .build()
                ).build(),
            sourceParams.get("regexField")
        );

        // @GreaterThan
        assertEquals(
            Specifier.Parameter.newBuilder()
                .setType(Specifier.Parameter.Type.TYPE_INT)
                .setDefault("101")
                .addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_GREATER_THAN)
                        .setValue("100")
                        .build()
                ).build(),
            sourceParams.get("intFieldGreaterThan")
        );

        // @LessThan
        assertEquals(
            Specifier.Parameter.newBuilder()
                .setType(Specifier.Parameter.Type.TYPE_INT)
                .setDefault("199")
                .addValidations(
                    Specifier.Parameter.Validation.newBuilder()
                        .setType(Specifier.Parameter.Validation.Type.TYPE_LESS_THAN)
                        .setValue("200")
                        .build()
                ).build(),
            sourceParams.get("longFieldLessThan")
        );

        // @LessThan
        assertEquals(
            Specifier.Parameter.newBuilder()
                .setType(Specifier.Parameter.Type.TYPE_STRING)
                .build(),
            sourceParams.get("nestedConfig.nestedStringConfig")
        );
    }

    @AllArgsConstructor
    public static class SourceConfig {
        @Pattern(regexp = "abc.*def")
        @Required
        String regexField;

        @Default("101")
        @Min(100)
        int intFieldGreaterThan;

        @Default("199")
        @Max(200)
        long longFieldLessThan;

        @Valid
        SourceNestedConfig nestedConfig;
    }

    @Data
    @NoArgsConstructor
    public static class SourceNestedConfig {
        String nestedStringConfig;
    }
}
