package com.circonus.metrics.circonus.transport;

import com.circonus.metrics.circonus.model.CirconusCounter;
import com.circonus.metrics.circonus.model.CirconusGauge;
import com.circonus.metrics.circonus.model.CirconusHistogram;

import java.io.Closeable;
import java.io.IOException;

/**
 * The transport layer for pushing metrics to Circonus
 */
public interface Transport extends Closeable {

  /**
   * Build a request context.
   */
  public Request prepare() throws IOException;

  /**
   * A request for batching of metrics to be pushed to Circonus.
   * The call order is expected to be:
   *    one or more of addGauge, addCounter -> send()
   */
  public interface Request {

    /**
     * Add a gauge
     */
    void addGauge(CirconusGauge gauge) throws IOException;

    /**
     * Add a counter to the request
     */
    void addCounter(CirconusCounter counter) throws IOException;

    /**
     * Add a counter to the request
     */
    void addHistogram(CirconusHistogram hist) throws IOException;

    /**
     * Send the request to Circonus
     */
    void send() throws Exception;
  }
}
