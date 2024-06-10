package io.conduit.sdk;

import io.conduit.grpc.Destination;
import io.conduit.grpc.DestinationPluginGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@GrpcService
public class DestinationService extends DestinationPluginGrpc.DestinationPluginImplBase {
    private static final Logger logger = Logger.getLogger(DestinationService.class);

    // Using Instance<> so that it's possible that
    // a Source or Destination are not implemented
    @Inject
    Instance<io.conduit.sdk.Destination> destination;

    private io.conduit.sdk.Destination getDestination() {
        if (destination.isUnsatisfied()) {
            throw new IllegalArgumentException("destination not implemented");
        }
        return destination.get();
    }

    @Override
    public void configure(Destination.Configure.Request request,
                          StreamObserver<Destination.Configure.Response> responseObserver) {
        logger.info("DestinationService::configure");

        try {
            var cfgMap = ConfigUtils.validateAndFlatten(getDestination().configClass(), request.getConfigMap());
            getDestination().configure(cfgMap);

            getDestination().configure(request.getConfigMap());
            responseObserver.onNext(Destination.Configure.Response.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("failed configuring destination", e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("couldn't configure task: " + e)
                    .withCause(e)
                    .asException()
            );
        }

    }

    @Override
    public void start(Destination.Start.Request request,
                      StreamObserver<Destination.Start.Response> responseObserver) {
        logger.info("DestinationService::start");

        try {
            getDestination().open();
            responseObserver.onNext(Destination.Start.Response.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("failed starting destination", e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("couldn't start destination: " + e)
                    .withCause(e)
                    .asException()
            );
        }

    }

    @Override
    @Blocking
    public StreamObserver<Destination.Run.Request> run(StreamObserver<Destination.Run.Response> responseObserver) {
        logger.info("DestinationService::run");

        return new DestinationStream(getDestination(), responseObserver);
    }

    @Override
    public void stop(Destination.Stop.Request request,
                     StreamObserver<Destination.Stop.Response> responseObserver) {
        logger.info("DestinationService::stop");

        responseObserver.onNext(Destination.Stop.Response.newBuilder().build());
        responseObserver.onCompleted();

    }

    @Override
    public void teardown(Destination.Teardown.Request request,
                         StreamObserver<Destination.Teardown.Response> responseObserver) {
        logger.info("DestinationService::teardown");
        responseObserver.onNext(Destination.Teardown.Response.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void lifecycleOnCreated(Destination.Lifecycle.OnCreated.Request request,
                                   StreamObserver<Destination.Lifecycle.OnCreated.Response> responseObserver) {
        System.out.println("DestinationService::lifecycleOnCreated");
        responseObserver.onNext(Destination.Lifecycle.OnCreated.Response.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void lifecycleOnUpdated(Destination.Lifecycle.OnUpdated.Request request,
                                   StreamObserver<Destination.Lifecycle.OnUpdated.Response> responseObserver) {
        System.out.println("DestinationService::lifecycleOnUpdated");
        responseObserver.onNext(Destination.Lifecycle.OnUpdated.Response.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void lifecycleOnDeleted(Destination.Lifecycle.OnDeleted.Request request, StreamObserver<Destination.Lifecycle.OnDeleted.Response> responseObserver) {
        System.out.println("DestinationService::lifecycleOnDeleted");
        responseObserver.onNext(Destination.Lifecycle.OnDeleted.Response.newBuilder().build());
        responseObserver.onCompleted();
    }
}
