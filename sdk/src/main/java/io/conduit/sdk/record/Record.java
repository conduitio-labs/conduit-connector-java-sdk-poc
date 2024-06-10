package io.conduit.sdk.record;

import java.util.Map;

import com.google.protobuf.ByteString;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Record {
    // Operation defines what triggered the creation of a record.
    public enum Operation {
        UNSPECIFIED,
        // Records with operation create contain data of a newly created entity.
        CREATE,
        // Records with operation update contain data of an updated entity.
        UPDATE,
        // Records with operation delete contain data of a deleted entity.
        DELETE,
        // Records with operation snapshot contain data of a previously existing
        // entity, fetched as part of a snapshot.
        SNAPSHOT;
    }

    private final Position position;

    private final Operation operation;
    private final Map<String, String> metadata;
    private final Data key;
    private final Change payload;

    public static Record fromGRPC(io.conduit.grpc.Record grpcRecord) {
        if (grpcRecord == null) {
            return null;
        }

        return Record.builder()
            .position(toSDKPosition(grpcRecord.getPosition()))
            .metadata(grpcRecord.getMetadataMap())
            .operation(toSDKOperation(grpcRecord.getOperation()))
            .key(toSDKData(grpcRecord.getKey()))
            .payload(toSDKChange(grpcRecord.getPayload()))
            .build();
    }

    private static Change toSDKChange(io.conduit.grpc.Change payload) {
        return new Change(toSDKData(payload.getBefore()), toSDKData(payload.getAfter()));
    }

    private static Data toSDKData(io.conduit.grpc.Data grpcData) {
        if (grpcData == null) {
            return () -> new byte[0];
        }

        return () -> grpcData.toByteArray();
    }

    private static Position toSDKPosition(ByteString position) {
        // todo hacky
        if (position == null) {
            return () -> new byte[0];
        }

        return () -> position.toByteArray();
    }

    private static Record.Operation toSDKOperation(io.conduit.grpc.Operation operation) {
        Map<io.conduit.grpc.Operation, Operation> m = Map.of(
            io.conduit.grpc.Operation.OPERATION_UNSPECIFIED, Operation.UNSPECIFIED,
            io.conduit.grpc.Operation.OPERATION_CREATE, Operation.CREATE,
            io.conduit.grpc.Operation.OPERATION_UPDATE, Operation.UPDATE,
            io.conduit.grpc.Operation.OPERATION_DELETE, Operation.DELETE,
            io.conduit.grpc.Operation.OPERATION_SNAPSHOT, Operation.SNAPSHOT
        );

        return m.get(operation);
    }

    public static io.conduit.grpc.Record toGRPC(Record rec) {
        return io.conduit.grpc.Record
            .newBuilder()
            .setKey(toGRPCData(rec.getKey()))
            .setPosition(ByteString.copyFrom(rec.getPosition().bytes()))
            .setPayload(toGRPCChange(rec.getPayload()))
            .build();
    }

    private static io.conduit.grpc.Change toGRPCChange(Change payload) {
        return io.conduit.grpc.Change.newBuilder()
            .setAfter(toGRPCData(payload.getAfter()))
            .build();
    }

    private static io.conduit.grpc.Data toGRPCData(Data sdkData) {
        return io.conduit.grpc.Data.newBuilder()
            .setRawData(ByteString.copyFrom(sdkData.bytes()))
            .build();
    }
}
