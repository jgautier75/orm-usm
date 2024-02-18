package com.acme.users.mgt.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 1000,TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        PublishSubscribeChannel exportChannel = new PublishSubscribeChannel(executorService);
        exportChannel.setErrorHandler(errorHandler);
        return exportChannel;
    }
}
