package com.acme.users.mgt.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;

import com.acme.users.mgt.events.EventBusErrorHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EventBusConfig {
    private final EventBusErrorHandler errorHandler;

    @Bean
    public PublishSubscribeChannel eventAuditChannel() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        PublishSubscribeChannel exportChannel = new PublishSubscribeChannel(executorService);
        exportChannel.setErrorHandler(errorHandler);
        return exportChannel;
    }
}
