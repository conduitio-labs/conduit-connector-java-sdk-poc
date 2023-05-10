package io.conduit.sdk;

public interface Connector {
    Specification specification();

    Source source();

    Destination destination();
}
