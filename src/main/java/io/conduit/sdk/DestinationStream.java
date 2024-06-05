package io.conduit.sdk;

import java.util.List;

import io.conduit.grpc.Destination;
import io.conduit.sdk.record.Record;
import io.grpc.stub.StreamObserver;
import org.jboss.logging.Logger;

public class DestinationStream implements StreamObserver<Destination.Run.Request> {
    private static final Logger logger = Logger.getLogger(DestinationStream.class);

    private final io.conduit.sdk.Destination destination;
    private final StreamObserver<Destination.Run.Response> responseObserver;

    public DestinationStream(io.conduit.sdk.Destination destination,
                             StreamObserver<Destination.Run.Response> responseObserver) {
        this.destination = destination;
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(Destination.Run.Request request) {
        // todo batching
        WriteResult result = destination.write(
            List.of(Record.fromGRPC(request.getRecord()))
        );
        Destination.Run.Response.Builder responseB = Destination.Run.Response.newBuilder()
            .setAckPosition(request.getRecord().getPosition());

        if (result.getError() != null) {
            responseB.setError(result.getErrorString());
        }

        responseObserver.onNext(responseB.build());
    }

    @Override
    public void onError(Throwable throwable) {
        logger.warn("onError called", throwable);
        responseObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        logger.info("onCompleted called");
        responseObserver.onCompleted();
    }
}
