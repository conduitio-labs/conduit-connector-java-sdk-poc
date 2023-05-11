package io.conduit.sdk;

import io.conduit.grpc.Specifier;
import io.conduit.grpc.SpecifierPluginGrpc;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import static java.util.Collections.emptyMap;

@GrpcService
public class SpecService extends SpecifierPluginGrpc.SpecifierPluginImplBase {
    @Inject
    private Instance<Specification> specification;

    @Override
    public void specify(Specifier.Specify.Request request,
                        StreamObserver<Specifier.Specify.Response> responseObserver) {

        responseObserver.onNext(
            Specifier.Specify.Response.newBuilder()
                .setName(getSpecification().name())
                .setSummary(getSpecification().summary())
                .setDescription(getSpecification().description())
                .setVersion(getSpecification().version())
                .setAuthor(getSpecification().author())
                .putAllDestinationParams(emptyMap())
                .putAllSourceParams(emptyMap())
                .build()
        );
        responseObserver.onCompleted();
    }

    private Specification getSpecification() {
        if (specification.isUnsatisfied()) {
            throw new IllegalArgumentException("specification not implemented");
        }

        return specification.get();
    }
}
