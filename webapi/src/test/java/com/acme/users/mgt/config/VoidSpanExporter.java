package com.acme.users.mgt.config;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class VoidSpanExporter implements SpanExporter{

    private static final SpanExporter INSTANCE =new VoidSpanExporter();

    static SpanExporter getInstance() {
        return INSTANCE;
     }

    @Override
    public CompletableResultCode export(Collection<SpanData> arg0) {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    
}
