package com.acme.users.mgt.config;

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
        PublishSubscribeChannel exportChannel = new PublishSubscribeChannel();
        exportChannel.setErrorHandler(errorHandler);
        return exportChannel;
    }
}
