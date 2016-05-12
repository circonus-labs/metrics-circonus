package com.circonus.metrics;

import com.circonus.metrics.HistImpl;

public interface HistImplContainer {
  public HistImpl getHistImpl();
}
