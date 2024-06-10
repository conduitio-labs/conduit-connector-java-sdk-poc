package io.conduit.sdk;

import io.conduit.grpc.Destination;
import io.conduit.grpc.DestinationPluginGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TestClient {
    public static void main(String[] args) {
        // Create a channel to the server.
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9000)
                .usePlaintext()
                .build();

        // Create a Greeter client from the channel.
        var stub = DestinationPluginGrpc.newBlockingStub(channel);

        // Call the SayHello method with a HelloRequest.
        var request = Destination.Configure.Request.newBuilder()
                .build();
        var response = stub.configure(request);

        // Print the response message.
        System.out.println("Response: " + response);

        // Shut down the channel.
        channel.shutdown();
    }
}
