package io.conduit.sdk;

import io.conduit.grpc.Source;
import io.conduit.grpc.SourcePluginGrpc;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@GrpcService
public class SourceService extends SourcePluginGrpc.SourcePluginImplBase {
    // Using Instance<> so that it's possible that
    // a Source or Destination are not implemented
    @Inject
    Instance<Destination> destination;

    @Override
    public void configure(Source.Configure.Request request, StreamObserver<Source.Configure.Response> responseObserver) {
        super.configure(request, responseObserver);
    }

    @Override
    public void start(Source.Start.Request request, StreamObserver<Source.Start.Response> responseObserver) {
        super.start(request, responseObserver);
    }

    @Override
    public StreamObserver<Source.Run.Request> run(StreamObserver<Source.Run.Response> responseObserver) {
        return super.run(responseObserver);
    }

    @Override
    public void stop(Source.Stop.Request request, StreamObserver<Source.Stop.Response> responseObserver) {
        super.stop(request, responseObserver);
    }

    @Override
    public void teardown(Source.Teardown.Request request, StreamObserver<Source.Teardown.Response> responseObserver) {
        super.teardown(request, responseObserver);
    }

    @Override
    public void lifecycleOnCreated(Source.Lifecycle.OnCreated.Request request, StreamObserver<Source.Lifecycle.OnCreated.Response> responseObserver) {
        super.lifecycleOnCreated(request, responseObserver);
    }

    @Override
    public void lifecycleOnUpdated(Source.Lifecycle.OnUpdated.Request request, StreamObserver<Source.Lifecycle.OnUpdated.Response> responseObserver) {
        super.lifecycleOnUpdated(request, responseObserver);
    }

    @Override
    public void lifecycleOnDeleted(Source.Lifecycle.OnDeleted.Request request, StreamObserver<Source.Lifecycle.OnDeleted.Response> responseObserver) {
        super.lifecycleOnDeleted(request, responseObserver);
    }
}
