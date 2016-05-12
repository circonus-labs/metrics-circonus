package com.circonus.metrics.serializer;

import com.circonus.metrics.model.CirconusCounter;
import com.circonus.metrics.model.CirconusGauge;
import com.circonus.metrics.model.CirconusHistogram;

import java.io.IOException;

/**
 * This defines the interface to build a Circonus request body.
 * The call order is expected to be:
 *   startObject() -> One or more of appendGauge/appendCounter -> endObject()
 * Note that this is a single-use class and nothing can be appended once endObject() is called.
 */
public interface Serializer {

  /**
   * Write starting marker of the Circonus time series object
   */
  public void startObject() throws IOException;

  /**
   * Append a gauge to the time series
   */
  public void appendGauge(CirconusGauge gauge) throws IOException;

  /**
   * Append a counter to the time series
   */
  public void appendCounter(CirconusCounter counter) throws IOException;

  /**
   * Append a histogram to the time series
   */
  public void appendHistogram(CirconusHistogram hist) throws IOException;

  /**
   * Mark ending of the circonus time series object
   */
  public void endObject() throws IOException;

  /**
   * Get Circonus time series object serialized as a string
   */
  public String getAsString() throws IOException;
}
