package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.coursera.metrics.datadog.DatadogReporter;
import org.coursera.metrics.datadog.transport.HttpTransport;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonTypeName("datadog")
public class DatadogReporterFactory extends BaseReporterFactory {
  @NotNull
  @JsonProperty
  private String host = null;

  @NotNull
  @JsonProperty
  private String apiKey = null;

  @JsonProperty
  private List<String> tags = null;

  @JsonProperty
  private int connectTimeout = 5000;  // in milliseconds

  @JsonProperty
  private int socketTimeout = 5000;   // in milliseconds

  public ScheduledReporter build(MetricRegistry registry) {
    HttpTransport transport = new HttpTransport.Builder()
        .withApiKey(apiKey)
        .withConnectTimeout(connectTimeout)
        .withSocketTimeout(socketTimeout)
        .build();

    return DatadogReporter.forRegistry(registry)
        .withTransport(transport)
        .withHost(host)
        .withTags(tags)
        .filter(getFilter())
        .convertDurationsTo(getDurationUnit())
        .convertRatesTo(getRateUnit())
        .build();
    }
}