package org.coursera.metrics.datadog;

public interface MetricNameFormatter {

  public String format(String name, String... path);
}
