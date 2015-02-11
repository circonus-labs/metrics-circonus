package org.coursera.metrics.datadog;

import java.util.List;

public interface DynamicTagsCallback {
  public List<String> getTags();
}
