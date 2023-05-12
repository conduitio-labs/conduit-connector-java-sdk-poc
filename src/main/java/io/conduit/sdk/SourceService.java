package io.conduit.sdk;

import com.google.protobuf.ByteString;
import io.conduit.grpc.Source;
import io.conduit.grpc.SourcePluginGrpc;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import static io.conduit.sdk.Utils.newPosition;

@GrpcService
public class SourceService extends SourcePluginGrpc.SourcePluginImplBase {
    private static final Logger logger = Logger.getLogger(SourceService.class);

    // Using Instance<> so that it's possible that
    // a Source or Destination are not implemented
    @Inject
    Instance<io.conduit.sdk.Source> source;

    private io.conduit.sdk.Source getSource() {
        if (source.isUnsatisfied()) {
            throw new IllegalArgumentException("source not implemented");
        }

        return source.get();
    }

    @Override
    public void configure(Source.Configure.Request request, StreamObserver<Source.Configure.Response> responseObserver) {
        try {
            logger.info("configuring source");
            getSource().configure(request.getConfigMap());
            responseObserver.onNext(Source.Configure.Response.newBuilder().build());
        } catch (Exception e) {
            logger.error("failed configuring source", e);
            responseObserver.onError(e);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void start(Source.Start.Request request, StreamObserver<Source.Start.Response> responseObserver) {
        try {
            logger.info("starting source");
            getSource().open(newPosition(request.getPosition()));
        } catch (Exception e) {
            logger.error("failed opening source", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<Source.Run.Request> run(StreamObserver<Source.Run.Response> responseObserver) {
        logger.info("running source");

        try {
            return new SourceStream(getSource(), responseObserver);
        } catch (Exception e) {
            logger.error("failed running source stream", e);
            responseObserver.onError(e);
            // todo handle better
            throw e;
        }
    }

    @Override
    public void stop(Source.Stop.Request request, StreamObserver<Source.Stop.Response> responseObserver) {
        // todo return last position read
        logger.info("stopping source");

        responseObserver.onNext(Source.Stop.Response.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void teardown(Source.Teardown.Request request, StreamObserver<Source.Teardown.Response> responseObserver) {
        try {
            logger.info("source teardown called");
            getSource().teardown();
            responseObserver.onNext(Source.Teardown.Response.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("teardown failed", e);
            responseObserver.onError(e);
        }
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
