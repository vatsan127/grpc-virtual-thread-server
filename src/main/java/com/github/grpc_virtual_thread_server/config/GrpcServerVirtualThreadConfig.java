package com.github.grpc_virtual_thread_server.config;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class GrpcServerVirtualThreadConfig {

    @Bean
    public GrpcServerConfigurer virtualThreadServerConfigurer() {
        return serverBuilder -> {
            log.info("Configuring gRPC server executor = newVirtualThreadPerTaskExecutor (carriers={})",
                    Runtime.getRuntime().availableProcessors());
            serverBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
