package com.circonus.metrics;

public interface MetricNameFormatter {

  public String format(String name, String... path);
}
