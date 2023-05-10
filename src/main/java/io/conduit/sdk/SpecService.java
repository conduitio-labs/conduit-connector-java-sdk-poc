package io.conduit.sdk;

import io.conduit.grpc.Specifier;
import io.conduit.grpc.SpecifierPluginGrpc;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;

import static java.util.Collections.emptyMap;

@GrpcService
public class SpecService extends SpecifierPluginGrpc.SpecifierPluginImplBase {
    @Inject
    private Specification specification;

    @Override
    public void specify(Specifier.Specify.Request request,
                        StreamObserver<Specifier.Specify.Response> responseObserver) {

        responseObserver.onNext(
            Specifier.Specify.Response.newBuilder()
                .setName(specification.name())
                .setSummary(specification.summary())
                .setDescription(specification.description())
                .setVersion(specification.version())
                .setAuthor(specification.author())
                .putAllDestinationParams(emptyMap())
                .putAllSourceParams(emptyMap())
                .build()
        );
        responseObserver.onCompleted();
    }
}
