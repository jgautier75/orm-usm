package com.acme.users.mgt.pojo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MetricsConfig {
    private List<MetricDefinition> metricsDefinitions;
}
