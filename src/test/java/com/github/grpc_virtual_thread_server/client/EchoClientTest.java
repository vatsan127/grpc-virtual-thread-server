package com.github.grpc_virtual_thread_server.client;

import com.github.grpc_virtual_thread_server.grpc.EchoReply;
import com.github.grpc_virtual_thread_server.grpc.EchoRequest;
import com.github.grpc_virtual_thread_server.grpc.EchoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class EchoClientTest {

    @Test
    void singleEcho() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        try {
            EchoServiceGrpc.EchoServiceBlockingStub stub = EchoServiceGrpc.newBlockingStub(channel);
            EchoReply reply = stub.echo(EchoRequest.newBuilder()
                    .setMessage("hello VT")
                    .setDelayMillis(200)
                    .build());
            System.out.println("REPLY message   = " + reply.getMessage());
            System.out.println("REPLY thread    = " + reply.getThreadName());
            System.out.println("REPLY isVirtual = " + reply.getVirtualThread());
            System.out.println("REPLY delayMs   = " + reply.getDelayMillis());
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
