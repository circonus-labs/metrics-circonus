package com.circonus.metrics;

import java.util.List;

/**
 * An implementation of this interface can be used to pass a callback to the builder of
 * {@link com.circonus.metrics.CirconusReporter CirconusReporter}, so that CirconusReporter
 * can use dynamic tags
 */
public interface DynamicTagsCallback {
  /**
   *
   * @return dynamic tags that will merge into the static tags. Dynamic tags will overwrite
   * static tags with the same key
   */
  public List<String> getTags();
}
