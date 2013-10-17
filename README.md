# Metrics Datadog Reporter
Simple Metrics reporter that sends The Goods to Datadog. Real person
documentation pending

## Usage

~~~scala
import com.yammer.metrics.reporting.DatadogReporter

...

DatadogReporter.enable(15, TimeUnit.SECONDS, myDatadogKey)
~~~
