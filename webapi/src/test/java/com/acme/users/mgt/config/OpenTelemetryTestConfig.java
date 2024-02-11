package com.acme.users.mgt.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ResourceAttributes;

public class OpenTelemetryTestConfig {

        @Bean
        @Qualifier("otelResourceTest")
        public Resource otelResourceTest() {
                return Resource.getDefault().toBuilder()
                                .put(ResourceAttributes.SERVICE_NAME, "TEST")
                                .put(ResourceAttributes.SERVICE_VERSION, "1.0.0").build();
        }

        @Bean
        @Qualifier("tracerProviderTest")
        public SdkTracerProvider tracerProviderTest(@Qualifier("otelResourceTest") Resource otelResource) {
                SpanExporter spanExporter = VoidSpanExporter.getInstance();
                return SdkTracerProvider.builder()
                                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                                                .setScheduleDelay(Duration.ofSeconds(5)).build())
                                // .setResource(otelResource)
                                .build();
        }

        @Bean
        @Qualifier("sdkMeterProviderTest")
        public SdkMeterProvider sdkMeterProviderTest(@Qualifier("otelResourceTest") Resource otelResource) {
                MetricExporter metricExporter = new VoidMetricExporter();
                return SdkMeterProvider.builder()
                                .registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                                                .setInterval(Duration.ofSeconds(30)).build())
                                // .setResource(otelResource)
                                .build();
        }

        @Bean
        @Qualifier("sdkLoggerProviderTest")
        public SdkLoggerProvider sdkLoggerProviderTest(@Qualifier("otelResourceTest") Resource otelResource) {
                VoidLogRecordExporter logRecordExporter = new VoidLogRecordExporter();
                return SdkLoggerProvider.builder()
                                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logRecordExporter)
                                                .setScheduleDelay(Duration.ofSeconds(5)).build())
                                .addResource(otelResource)
                                .build();
        }

        /*-
        @Bean
        public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider, SdkMeterProvider sdkMeterProvider,
                        SdkLoggerProvider sdkLoggerProvider) {
                Resource resource = Resource.getDefault().toBuilder()
                                .put(ResourceAttributes.SERVICE_NAME, "TEST")
                                .put(ResourceAttributes.SERVICE_VERSION, "1.0.0").build();
        
                // OpenTelemetry
                OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                                .setTracerProvider(tracerProvider)
                                .setMeterProvider(sdkMeterProvider)
                                .setLoggerProvider(sdkLoggerProvider)
                                .setPropagators(ContextPropagators.create(TextMapPropagator
                                                .composite(W3CTraceContextPropagator.getInstance(),
                                                                W3CBaggagePropagator.getInstance())))
                                .buildAndRegisterGlobal();
        
                return openTelemetry;
        
        }*/

}
