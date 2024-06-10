package io.conduit.connectors.file;

import io.conduit.sdk.specification.Specification;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileSpec implements Specification {
    @Override
    public String name() {
        return "file-java";
    }

    @Override
    public String summary() {
        return "a file connector written in java";
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
