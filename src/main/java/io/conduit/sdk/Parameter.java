package io.conduit.sdk;

import lombok.Builder;

import java.util.List;

@Builder
public class Parameter {
    // Default is the default value of the parameter, if any.
    private final String defaultVal;
    // Required controls if the parameter will be shown as required or optional.
    // Deprecated: add ValidationRequired to Parameter.Validations instead.
    private final boolean required;
    // Description holds a description of the field and how to configure it.
    private final String description;
    // Type defines the parameter data type.
    private final ParameterType type;
    // Validations slice of validations to be checked for the parameter.
    private final List<Validation> validations;
}
