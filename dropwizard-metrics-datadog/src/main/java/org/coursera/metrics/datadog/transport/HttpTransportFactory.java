package org.coursera.metrics.datadog.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;

@JsonTypeName("http")
public class HttpTransportFactory implements AbstractTransportFactory {

  @NotNull
  @JsonProperty
  private String apiKey = null;

  @JsonProperty
  private Duration connectTimeout = Duration.seconds(5);

  @JsonProperty
  private Duration socketTimeout = Duration.seconds(5);

  public HttpTransport build() {
    return new HttpTransport.Builder()
        .withApiKey(apiKey)
        .withConnectTimeout((int) connectTimeout.toMilliseconds())
        .withSocketTimeout((int) socketTimeout.toMilliseconds())
        .build();
  }
}
