# Metrics Datadog Reporter
Simple Metrics reporter that sends reporting info to Datadog.

## Usage

~~~scala
import com.yammer.metrics.reporting.DatadogReporter

...

DatadogReporter.enable(15, TimeUnit.SECONDS, myDatadogKey)
~~~


## Maven Info

Metrics datadog reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.coursera%22%20AND%20a%3A%22metrics-datadog%22)

* Group: org.coursera
* Artifact: metrics-datadog
* Version: 0.1.5
