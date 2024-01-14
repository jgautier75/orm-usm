package com.acme.users.mgt.config;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MicrometerPrometheus implements InitializingBean {
    private final MeterRegistry meterRegistry;
    private final AppGenericConfig appGenericConfig;
    private final AtomicLong techErrorsCounter = new AtomicLong();

    @Override
    public void afterPropertiesSet() throws Exception {
        Gauge.builder("technical-errors", techErrorsCounter::get).description("Technical errors gauge")
                .tag("module", appGenericConfig.getModuleName()).register(meterRegistry);
    }

    public AtomicLong getTechErrorsCoounter() {
        return techErrorsCounter;
    }

}
