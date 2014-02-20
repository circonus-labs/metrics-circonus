# Metrics Datadog Reporter
Simple Metrics reporter that sends reporting info to Datadog.

## Usage

~~~scala
import com.yammer.metrics.reporting.DatadogReporter

...

DatadogReporter.enable(15, TimeUnit.SECONDS, myDatadogKey)
~~~


## Maven Info

* Group: org.coursera
* Artifact: metrics-datadog
* Version: 0.1.5
