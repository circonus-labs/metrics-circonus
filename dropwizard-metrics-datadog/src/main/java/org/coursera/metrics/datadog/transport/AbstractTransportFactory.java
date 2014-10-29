package org.coursera.metrics.datadog.transport;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(HttpTransportFactory.class),
        @JsonSubTypes.Type(UdpTransportFactory.class)
})
public interface AbstractTransportFactory {
  public Transport build();
}
