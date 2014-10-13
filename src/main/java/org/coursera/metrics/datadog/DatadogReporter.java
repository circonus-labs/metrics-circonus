package org.coursera.metrics.datadog;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.coursera.metrics.datadog.transport.Transport;
import org.coursera.metrics.datadog.model.DatadogCounter;
import org.coursera.metrics.datadog.model.DatadogGauge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class DatadogReporter extends ScheduledReporter {

  private static final Logger LOG =
      LoggerFactory.getLogger(DatadogReporter.class);
  private final Transport transport;
  private final Clock clock;
  private final String host;
  private final EnumSet<Expansion> expansions;
  private final MetricNameFormatter metricNameFormatter;
  private final List<String> tags;
  private Transport.Request request;

  private DatadogReporter(MetricRegistry metricRegistry,
                          Transport transport,
                          MetricFilter filter,
                          Clock clock,
                          String host,
                          EnumSet<Expansion> expansions,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          MetricNameFormatter metricNameFormatter,
                          List<String> tags) {
    super(metricRegistry, "datadog-reporter", filter, rateUnit, durationUnit);
    this.clock = clock;
    this.host = host;
    this.expansions = expansions;
    this.metricNameFormatter = metricNameFormatter;
    this.tags = tags;
    this.transport = transport;
  }

  @Override
  public void report(SortedMap<String, Gauge> gauges,
                     SortedMap<String, Counter> counters,
                     SortedMap<String, Histogram> histograms,
                     SortedMap<String, Meter> meters,
                     SortedMap<String, Timer> timers) {
    final long timestamp = clock.getTime() / 1000;

    try {
      request = transport.prepare();

      for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
        reportGauge(entry.getKey(), entry.getValue(), timestamp);
      }

      for (Map.Entry<String, Counter> entry : counters.entrySet()) {
        reportCounter(entry.getKey(), entry.getValue(), timestamp);
      }

      for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
        reportHistogram(entry.getKey(), entry.getValue(), timestamp);
      }

      for (Map.Entry<String, Meter> entry : meters.entrySet()) {
        reportMetered(entry.getKey(), entry.getValue(), timestamp);
      }

      for (Map.Entry<String, Timer> entry : timers.entrySet()) {
        reportTimer(entry.getKey(), entry.getValue(), timestamp);
      }

      request.send();
    } catch (Exception e) {
      LOG.error("Error reporting metrics to Datadog", e);
    }
  }

  private void reportTimer(String name, Timer timer, long timestamp)
      throws IOException {
    final Snapshot snapshot = timer.getSnapshot();

    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MAX, name),
                     toNumber(convertDuration(snapshot.getMax())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MEAN, name),
                     toNumber(convertDuration(snapshot.getMean())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MIN, name),
                     toNumber(convertDuration(snapshot.getMin())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.STD_DEV, name),
                     toNumber(convertDuration(snapshot.getStdDev())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P50, name),
                     toNumber(convertDuration(snapshot.getMedian())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P75, name),
                     toNumber(convertDuration(snapshot.get75thPercentile())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P95, name),
                     toNumber(convertDuration(snapshot.get95thPercentile())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P98, name),
                     toNumber(convertDuration(snapshot.get98thPercentile())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P99, name),
                     toNumber(convertDuration(snapshot.get99thPercentile())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P999, name),
                     toNumber(convertDuration(snapshot.get999thPercentile())),
                     timestamp,
                     host,
                     tags));

    reportMetered(name, timer, timestamp);
  }

  private void reportMetered(String name, Metered meter, long timestamp)
      throws IOException {
    request.addCounter(new DatadogCounter(maybeExpand(Expansion.COUNT, name),
                       meter.getCount(),
                       timestamp,
                       host,
                       tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.RATE_1_MINUTE, name),
                     toNumber(convertRate(meter.getOneMinuteRate())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.RATE_5_MINUTE, name),
                     toNumber(convertRate(meter.getFiveMinuteRate())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.RATE_15_MINUTE, name),
                     toNumber(convertRate(meter.getFifteenMinuteRate())),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.RATE_MEAN, name),
                     toNumber(convertRate(meter.getMeanRate())),
                     timestamp,
                     host,
                     tags));
  }

  private void reportHistogram(String name, Histogram histogram, long timestamp)
      throws IOException {
    final Snapshot snapshot = histogram.getSnapshot();

    request.addCounter(new DatadogCounter(maybeExpand(Expansion.COUNT, name),
                       histogram.getCount(),
                       timestamp,
                       host,
                       tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MAX, name),
                     toNumber(snapshot.getMax()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MEAN, name),
                     toNumber(snapshot.getMean()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.MIN, name),
                     toNumber(snapshot.getMin()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.STD_DEV, name),
                     toNumber(snapshot.getStdDev()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P50, name),
                     toNumber(snapshot.getMedian()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P75, name),
                     toNumber(snapshot.get75thPercentile()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P95, name),
                     toNumber(snapshot.get95thPercentile()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P98, name),
                     toNumber(snapshot.get98thPercentile()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P99, name),
                     toNumber(snapshot.get99thPercentile()),
                     timestamp,
                     host,
                     tags));
    request.addGauge(new DatadogGauge(maybeExpand(Expansion.P999, name),
                     toNumber(snapshot.get999thPercentile()),
                     timestamp,
                     host,
                     tags));
  }

  private void reportCounter(String name, Counter counter, long timestamp)
      throws IOException {
    request.addCounter(new DatadogCounter(name, counter.getCount(), timestamp, host, tags));
  }

  private void reportGauge(String name, Gauge gauge, long timestamp)
      throws IOException {
    final Number value = toNumber(gauge.getValue());
    if (value != null) {
      request.addGauge(new DatadogGauge(name, value, timestamp, host, tags));
    }
  }

  private Number toNumber(Object o) {
    if (o instanceof Number) {
      return (Number) o;
    }
    return null;
  }

  private String maybeExpand(Expansion expansion, String name) {
    if (expansions.contains(expansion)) {
      return metricNameFormatter.format(name, expansion.toString());
    } else {
      return metricNameFormatter.format(name);
    }
  }

  public static enum Expansion {
    COUNT("count"),
    RATE_MEAN("meanRate"),
    RATE_1_MINUTE("1MinuteRate"),
    RATE_5_MINUTE("5MinuteRate"),
    RATE_15_MINUTE("15MinuteRate"),
    MIN("min"),
    MEAN("mean"),
    MAX("max"),
    STD_DEV("stddev"),
    MEDIAN("median"),
    P50("p50"),
    P75("p75"),
    P95("p95"),
    P98("p98"),
    P99("p99"),
    P999("p999");

    public static EnumSet<Expansion> ALL = EnumSet.allOf(Expansion.class);

    private final String displayName;

    private Expansion(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  public static Builder forRegistry(MetricRegistry registry) {
    return new Builder(registry);
  }

  public static class Builder {
    private final MetricRegistry registry;
    private String host;
    private EnumSet<Expansion> expansions;
    private Clock clock;
    private TimeUnit rateUnit;
    private TimeUnit durationUnit;
    private MetricFilter filter;
    private MetricNameFormatter metricNameFormatter;
    private List<String> tags;
    private Transport transport;

    public Builder(MetricRegistry registry) {
      this.registry = registry;
      this.expansions = Expansion.ALL;
      this.clock = Clock.defaultClock();
      this.rateUnit = TimeUnit.SECONDS;
      this.durationUnit = TimeUnit.MILLISECONDS;
      this.filter = MetricFilter.ALL;
      this.metricNameFormatter = new DefaultMetricNameFormatter();
      this.tags = new ArrayList<String>();
    }

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Builder withEC2Host() throws IOException {
      this.host = AwsHelper.getEc2InstanceId();
      return this;
    }

    public Builder withExpansions(EnumSet<Expansion> expansions) {
      this.expansions = expansions;
      return this;
    }

    public Builder convertRatesTo(TimeUnit rateUnit) {
      this.rateUnit = rateUnit;
      return this;
    }

    /**
     * Tags that would be sent to datadog with each and every metrics. This could be used to set
     * global metrics like version of the app, environment etc.
     * @param tags List of tags eg: [env:prod, version:1.0.1, name:kafka_client] etc
     */
    public Builder withTags(List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder withClock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public Builder filter(MetricFilter filter) {
      this.filter = filter;
      return this;
    }

    public Builder withMetricNameFormatter(MetricNameFormatter formatter) {
      this.metricNameFormatter = formatter;
      return this;
    }

    public Builder convertDurationsTo(TimeUnit durationUnit) {
      this.durationUnit = durationUnit;
      return this;
    }

    /**
     * The transport mechanism to push metrics to datadog. Supports http webservice and UDP
     * dogstatsd protocol as of now.
     *
     * @see org.coursera.metrics.datadog.transport.HttpTransport
     * @see org.coursera.metrics.datadog.transport.UdpTransport
     */
    public Builder withTransport(Transport transport) {
      this.transport = transport;
      return this;
    }

    public DatadogReporter build() {
      if (transport == null) {
        LOG.error("Transport is null. Please set a valid transport. " +
            "None of the metrics would get reported to datadog");
      }
      return new DatadogReporter(
          this.registry,
          this.transport,
          this.filter,
          this.clock,
          this.host,
          this.expansions,
          this.rateUnit,
          this.durationUnit,
          this.metricNameFormatter,
          this.tags);
    }
  }
}
