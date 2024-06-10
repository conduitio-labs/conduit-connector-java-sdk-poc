package io.conduit.connectors.generator;

import io.conduit.sdk.specification.Specification;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeneratorSpec implements Specification {
    @Override
    public String name() {
        return "generator-java";
    }

    @Override
    public String summary() {
        return "generator connector, written in java";
    }

    @Override
    public String description() {
        return summary();
    }

    @Override
    public String version() {
        return "v0.1.0";
    }

    @Override
    public String author() {
        return "Meroxa, Inc.";
    }
}
