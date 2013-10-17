package com.yammer.metrics.reporting;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.Transport.Request;
import com.yammer.metrics.reporting.model.DatadogCounter;
import com.yammer.metrics.reporting.model.DatadogGauge;
import com.yammer.metrics.stats.Snapshot;

public class DatadogReporter extends AbstractPollingReporter implements
    MetricProcessor<Long> {

  public boolean printVmMetrics = true;
  protected final Locale locale = Locale.US;
  protected final Clock clock;
  private final String host;
  protected final MetricPredicate predicate;
  protected final Transport transport;
  protected final EnumSet<Expansions> expansions;
  private static final Logger LOG = LoggerFactory
      .getLogger(DatadogReporter.class);
  private final VirtualMachineMetrics vm;

  private static final JsonFactory jsonFactory = new JsonFactory();
  private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);
  private JsonGenerator jsonOut;

  public DatadogReporter(MetricsRegistry metricsRegistry,
      MetricPredicate predicate, VirtualMachineMetrics vm, Transport transport,
      Clock clock, String host, EnumSet<Expansions> expansions, Boolean printVmMetrics) {
    super(metricsRegistry, "datadog-reporter");
    this.vm = vm;
    this.transport = transport;
    this.predicate = predicate;
    this.clock = clock;
    this.host = host;
    this.expansions = expansions;
    this.printVmMetrics = printVmMetrics;
  }

  @Override
  public void run() {
    Request request = null;
    try {
      request = transport.prepare();
      jsonOut = jsonFactory.createGenerator(request.getBodyWriter());
      jsonOut.writeStartObject();
      jsonOut.writeFieldName("series");
      jsonOut.writeStartArray();
    } catch (IOException ioe) {
      LOG.error("Could not prepare request", ioe);
      return;
    }

    final long epoch = clock.time() / 1000;
    if (this.printVmMetrics) {
      pushVmMetrics(epoch);
    }
    pushRegularMetrics(epoch);

    try {
      jsonOut.writeEndArray();
      jsonOut.writeEndObject();
      jsonOut.flush();
      request.send();
    } catch (Exception e) {
      LOG.error("Error sending metrics", e);
    }
  }

  public void processCounter(MetricName name, Counter counter, Long epoch)
      throws Exception {
    pushCounter(name, counter.count(), epoch);
  }

  public void processGauge(MetricName name, Gauge<?> gauge, Long epoch)
      throws Exception {
    pushGauge(name, (Number) gauge.value(), epoch);
  }

  public void processHistogram(MetricName name, Histogram histogram, Long epoch)
      throws Exception {
    pushSummarizable(name, histogram, epoch);
    pushSampling(name, histogram, epoch);
  }

  public void processMeter(MetricName name, Metered meter, Long epoch)
      throws Exception {
    if (expansions.contains(Expansions.COUNT))
      pushCounter(name, meter.count(), epoch, Expansions.COUNT.toString());

    maybeExpand(Expansions.RATE_MEAN, name, meter.meanRate(), epoch);
    maybeExpand(Expansions.RATE_1_MINUTE, name, meter.oneMinuteRate(), epoch);
    maybeExpand(Expansions.RATE_5_MINUTE, name, meter.fiveMinuteRate(), epoch);
    maybeExpand(Expansions.RATE_15_MINUTE, name, meter.fifteenMinuteRate(), epoch);
  }

  public void processTimer(MetricName name, Timer timer, Long epoch)
      throws Exception {
    processMeter(name, timer, epoch);
    pushSummarizable(name, timer, epoch);
    pushSampling(name, timer, epoch);
  }

  private void pushSummarizable(MetricName name, Summarizable summarizable,
      Long epoch) {
    maybeExpand(Expansions.MIN, name, summarizable.min(), epoch);
    maybeExpand(Expansions.MAX, name, summarizable.max(), epoch);
    maybeExpand(Expansions.MEAN, name, summarizable.mean(), epoch);
    maybeExpand(Expansions.STD_DEV, name, summarizable.stdDev(), epoch);
  }

  private void pushSampling(MetricName name, Sampling sampling, Long epoch) {
    final Snapshot snapshot = sampling.getSnapshot();
    maybeExpand(Expansions.MEDIAN, name, snapshot.getMedian(), epoch);
    maybeExpand(Expansions.P75, name, snapshot.get75thPercentile(), epoch);
    maybeExpand(Expansions.P95, name, snapshot.get95thPercentile(), epoch);
    maybeExpand(Expansions.P98, name, snapshot.get98thPercentile(), epoch);
    maybeExpand(Expansions.P99, name, snapshot.get99thPercentile(), epoch);
    maybeExpand(Expansions.P999, name, snapshot.get999thPercentile(), epoch);
  }

  private void maybeExpand(Expansions expansion, MetricName name, Number count, Long epoch) {
    if (expansions.contains(expansion))
      pushGauge(name, count, epoch, expansion.toString());
  }

  protected void pushRegularMetrics(long epoch) {
    for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
        .groupedMetrics(predicate).entrySet()) {
      for (Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
        final Metric metric = subEntry.getValue();
        if (metric != null) {
          try {
            metric.processWith(this, subEntry.getKey(), epoch);
          } catch (Exception e) {
            LOG.error("Error pushing metric", e);
          }
        }
      }
    }
  }

  protected void pushVmMetrics(long epoch) {
    sendGauge("jvm.memory.heap_usage", vm.heapUsage(), epoch);
    sendGauge("jvm.memory.non_heap_usage", vm.nonHeapUsage(), epoch);
    for (Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
      String gaugeName = String.format("jvm.memory.memory_pool_usage[pool:%s]",
          pool.getKey());

      sendGauge(gaugeName, pool.getValue(), epoch);
    }

    pushGauge("jvm.daemon_thread_count", vm.daemonThreadCount(), epoch);
    pushGauge("jvm.thread_count", vm.threadCount(), epoch);
    pushCounter("jvm.uptime", vm.uptime(), epoch);
    sendGauge("jvm.fd_usage", vm.fileDescriptorUsage(), epoch);

    for (Entry<Thread.State, Double> entry : vm.threadStatePercentages()
        .entrySet()) {
      String gaugeName = String.format("jvm.thread-states[state:%s]",
          entry.getKey());
      sendGauge(gaugeName, entry.getValue(), epoch);
    }

    for (Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm
        .garbageCollectors().entrySet()) {
      pushGauge("jvm.gc.time", entry.getValue().getTime(TimeUnit.MILLISECONDS), epoch);
      pushCounter("jvm.gc.runs", entry.getValue().getRuns(), epoch);
    }
  }

  private void pushCounter(MetricName metricName, Long count, Long epoch,
      String... path) {
    pushCounter(sanitizeName(metricName, path), count, epoch);

  }

  private void pushCounter(String name, Long count, Long epoch) {
    DatadogCounter counter = new DatadogCounter(name, count, epoch, host);
    try {
      mapper.writeValue(jsonOut, counter);
    } catch (Exception e) {
      LOG.error("Error writing counter", e);
    }
  }

  private void pushGauge(MetricName metricName, Number count, Long epoch,
      String... path) {
    sendGauge(sanitizeName(metricName, path), count, epoch);
  }

  private void pushGauge(String name, long count, long epoch) {
    sendGauge(name, new Long(count), epoch);
  }

  private void sendGauge(String name, Number count, Long epoch) {
    DatadogGauge gauge = new DatadogGauge(name, count, epoch, host);
    try {
      mapper.writeValue(jsonOut, gauge);
    } catch (Exception e) {
      LOG.error("Error writing gauge", e);
    }
  }

  protected String sanitizeName(MetricName name, String... path) {
    final StringBuilder sb = new StringBuilder(name.getGroup());
    sb.append('.');
    sb.append(name.getType()).append('.');

    if (name.hasScope()) {
      sb.append(name.getScope()).append('.');
    }

    String[] metricParts = name.getName().split("\\[");
    sb.append(metricParts[0]);

    for (String part : path) {
      sb.append('.').append(part);
    }

    for (int i = 1; i < metricParts.length; i++) {
      sb.append('[').append(metricParts[i]);
    }
    return sb.toString();
  }

  public static enum Expansions {
    COUNT("count"),
    RATE_MEAN("meanRate"),
    RATE_1_MINUTE("1MinuteRate"),
    RATE_5_MINUTE("1MinuteRate"),
    RATE_15_MINUTE("1MinuteRate"),
    MIN("min"),
    MEAN("mean"),
    MAX("max"),
    STD_DEV("stddev"),
    MEDIAN("median"),
    P75("p75"),
    P95("p95"),
    P98("p98"),
    P99("p99"),
    P999("p999");

    public static EnumSet<Expansions> ALL = EnumSet.allOf(Expansions.class);

    private final String displayName;

    private Expansions(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  public class Builder {
    private String host = null;
    private EnumSet<Expansions> expansions = Expansions.ALL;
    private Boolean vmMetrics = true;
    private String apiKey = null;
    private Clock clock = Clock.defaultClock();
    private MetricPredicate predicate = MetricPredicate.ALL;

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Builder withEC2Host() throws IOException {
      this.host = AwsHelper.getEc2InstanceId();
      return this;
    }

    public Builder withExpansions(EnumSet<Expansions> expansions) {
      this.expansions = expansions;
      return this;
    }

    public Builder withVmMetricsEnabled(Boolean enabled) {
      this.vmMetrics = enabled;
      return this;
    }

    public Builder withApiKey(String key) {
      this.apiKey = key;
      return this;
    }

    public Builder withClock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public Builder withPredicate(MetricPredicate predicate) {
      this.predicate = predicate;
      return this;
    }

    public DatadogReporter build() {
      return new DatadogReporter(
        Metrics.defaultRegistry(),
        this.predicate,
        VirtualMachineMetrics.getInstance(),
        new HttpTransport("app.datadoghq.com", apiKey),
        this.clock,
        this.host,
        this.expansions,
        this.vmMetrics);
    }
  }
}
