package com.acme.users.mgt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class MicrometerOpenTelemetryConfig {

    @Bean
    public OtlpGrpcSpanExporter otlpHttpSpanExporter(@Value("${app.telemetry}") String url) {
        return OtlpGrpcSpanExporter.builder().setEndpoint(url).build();
    }

}
