package org.coursera.metrics.datadog.transport;

import java.io.IOException;
import java.util.List;

public interface Transport {

  public Request prepare() throws IOException;

  public interface Request {

    void addGauge(String name,
                  Number value,
                  long timestamp,
                  String host,
                  List<String> additionalTags)
        throws IOException;
    void addCounter(String name,
                    Long value,
                    long timestamp,
                    String host,
                    List<String> additionalTags)
        throws IOException;
    void send() throws Exception;
  }
}
