package io.conduit.sdk;

import io.conduit.sdk.specification.Specification;

public interface Connector {
    Specification specification();

    Source source();

    Destination destination();
}
