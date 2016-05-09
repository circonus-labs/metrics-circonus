package com.circonus.metrics.circonus.transport;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(HttpTransportFactory.class)
})
public interface AbstractTransportFactory {
  public Transport build();
}
