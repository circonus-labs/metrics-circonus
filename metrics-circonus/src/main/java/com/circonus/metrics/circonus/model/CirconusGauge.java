package com.circonus.metrics.circonus.model;

import java.util.List;

public class CirconusGauge extends CirconusSeries<Number> {

  public CirconusGauge(String name, Number count, Long epoch, String host, List<String> additionalTags) {
    super(name, count, epoch, host, additionalTags);
  }

}
