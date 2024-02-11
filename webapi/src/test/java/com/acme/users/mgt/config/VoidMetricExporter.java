package com.acme.users.mgt.config;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class VoidMetricExporter implements MetricExporter {

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.DELTA;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        return new CompletableResultCode().succeed();
    }

    @Override
    public CompletableResultCode flush() {
        return new CompletableResultCode().succeed();
    }

    @Override
    public CompletableResultCode shutdown() {
        return new CompletableResultCode().succeed();
    }

}
