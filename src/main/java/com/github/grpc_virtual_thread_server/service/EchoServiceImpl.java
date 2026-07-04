package com.github.grpc_virtual_thread_server.service;

import com.github.grpc_virtual_thread_server.grpc.EchoReply;
import com.github.grpc_virtual_thread_server.grpc.EchoRequest;
import com.github.grpc_virtual_thread_server.grpc.EchoServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class EchoServiceImpl extends EchoServiceGrpc.EchoServiceImplBase {

    @Override
    public void echo(EchoRequest request, StreamObserver<EchoReply> responseObserver) {
        Thread current = Thread.currentThread();
        long delay = request.getDelayMillis();

        log.info("Echo received message='{}' delay={}ms on thread='{}' virtual={}",
                request.getMessage(), delay, current.getName(), current.isVirtual());

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
                return;
            }
        }

        EchoReply reply = EchoReply.newBuilder()
                .setMessage("Echo: " + request.getMessage())
                .setThreadName(current.getName())
                .setVirtualThread(current.isVirtual())
                .setDelayMillis(delay)
                .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
