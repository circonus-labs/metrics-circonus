  package com.circonus;
  import com.codahale.metrics.*;
  import java.util.concurrent.TimeUnit;
  import java.util.ArrayList;
  import com.circonus.metrics.HistogramAlaCoda;
  import com.circonus.metrics.CirconusReporter;
  import com.circonus.metrics.CirconusMetricRegistryAlaCoda;
  import com.circonus.metrics.transport.HttpTransport;

  public class Example {
    static final MetricRegistry metrics = new CirconusMetricRegistryAlaCoda();
    public static void main(String args[]) {
      startReport();
      Histogram latencies = metrics.histogram("latencies");
      latencies.update(123);
      latencies.update(1003);
      latencies.update(4503);
      latencies.update(4103);
      wait5Seconds();
    }

  static void startReport() {
      HttpTransport transport = (new HttpTransport.Builder())
        .withApiKey("54de4ecb-f04d-4ef0-9db3-f267f71eaa2c")
        .withCheckId("10000000-0000-0000-0000-000000000000")
        .withCheckSecret("0")
        .withBroker("127.0.0.1:43191")
        .build();
      CirconusReporter reporter = CirconusReporter.forRegistry(metrics)
          .withTransport(transport)
          .withTags(new ArrayList<String>() {{ add("version:1.0"); add("env:dev"); }})
          .convertRatesTo(TimeUnit.SECONDS)
          .convertDurationsTo(TimeUnit.MILLISECONDS)
          .onlyCirconusAnalytics(true)
          .build();
      reporter.start(1, TimeUnit.SECONDS);
  }

  static void wait5Seconds() {
      try {
          Thread.sleep(5*1000);
      }
      catch(InterruptedException e) {}
  }
}
