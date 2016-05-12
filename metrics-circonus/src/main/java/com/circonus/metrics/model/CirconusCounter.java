package com.circonus.metrics.model;

import java.util.List;

public class CirconusCounter extends CirconusSeries<Long> {
  
  public CirconusCounter(String name, Long count, Long epoch, String host, List<String> additionalTags) {
    super(name, count, epoch, host, additionalTags);
  }

}
