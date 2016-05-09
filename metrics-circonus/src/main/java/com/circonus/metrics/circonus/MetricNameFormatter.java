package com.circonus.metrics.circonus;

public interface MetricNameFormatter {

  public String format(String name, String... path);
}
