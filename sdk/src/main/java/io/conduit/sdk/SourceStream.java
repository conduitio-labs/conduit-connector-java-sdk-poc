package io.conduit.sdk;

import java.time.Duration;
import java.util.function.Supplier;

import io.conduit.grpc.Source;
import io.conduit.sdk.record.Record;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.grpc.stub.StreamObserver;
import org.jboss.logging.Logger;

import static io.conduit.sdk.Utils.newPosition;

public class SourceStream implements StreamObserver<Source.Run.Request> {
    private static final Logger logger = Logger.getLogger(SourceStream.class);

    private final io.conduit.sdk.Source source;
    private final StreamObserver<Source.Run.Response> responseObserver;
    private final RetryConfig retryCfg;

    public SourceStream(io.conduit.sdk.Source source,
                        StreamObserver<Source.Run.Response> responseObserver) {
        this.source = source;
        this.responseObserver = responseObserver;
        this.retryCfg = RetryConfig
            .custom()
            .intervalFunction(
                IntervalFunction.ofExponentialBackoff(
                    Duration.ofMillis(100),
                    2,
                    Duration.ofSeconds(5)
                )
            ).build();
        new Thread(this::runReader).start();
    }

    private void runReader() {
        logger.info("runReader() called");
        while (true) {
            try {
                logger.info("reading record...");
                Record rec = Retry.of("source.read", retryCfg).executeSupplier(source::read);
                logger.info("record read!");

                Source.Run.Response recResp = Source.Run.Response.newBuilder()
                    .setRecord(Record.toGRPC(rec))
                    .build();
                responseObserver.onNext(recResp);
                logger.info("record sent to stream");
            } catch (Exception e) {
                logger.error("failed reading record", e);
                responseObserver.onError(e);
            }
        }
    }

    @Override
    public void onNext(Source.Run.Request value) {
        source.ack(newPosition(value.getAckPosition()));
    }

    @Override
    public void onError(Throwable t) {
        logger.error("got an error", t);
        responseObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        logger.info("onCompleted called");
        responseObserver.onCompleted();
    }
}
