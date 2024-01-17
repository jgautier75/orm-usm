receivers:
  otlp:
    protocols:
      grpc:
      http:
processors:
  batch:

exporters:
  # OTLP
  otlp:
    endpoint: otel-collector:4317  
  # Data sources: traces, metrics, logs
  logging:
    loglevel: debug  
  # Prometheus
  prometheus:
    endpoint: "0.0.0.0:8889"

extensions:
  health_check:
  pprof:
  zpages:

service:
  extensions: [health_check,pprof,zpages]
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging, zipkin]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging, prometheus]

      
      