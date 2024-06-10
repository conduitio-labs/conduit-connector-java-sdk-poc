package com.meroxa.mysql;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Specification implements io.conduit.sdk.specification.Specification {
    @Override
    public String name() {
        return "mysql-java";
    }

    @Override
    public String summary() {
        return "mysql summary";
    }

    @Override
    public String description() {
        return "mysql description";
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
