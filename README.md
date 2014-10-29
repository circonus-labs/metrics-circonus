# Metrics Datadog Reporter
Simple Metrics reporter that sends reporting info to Datadog, supports both HTTP and UDP.

## Usage

~~~scala
import org.coursera.metrics.DatadogReporter
import org.coursera.metrics.DatadogReporter.Expansions._
import org.coursera.metrics.datadog.transport.Transport
import org.coursera.metrics.datadog.transport.HttpTransport
import org.coursera.metrics.datadog.transport.UdpTransport

...
val expansions = EnumSet.of(COUNT, RATE_1_MINUTE, RATE_15_MINUTE, MEDIAN, P95, P99)
val httpTransport = new HttpTransport.Builder().withApiKey(apiKey).build()
val reporter = DatadogReporter.forRegistry(registry)
  .withEC2Host()
  .withTransport(httpTransport)
  .withExpansions(expansions)
  .withMetricNameFormatter(ShortenedNameFormatter)
  .build()

reporter.start(10, TimeUnit.SECONDS)
~~~

Example of using UDP transport:

~~~scala
...
val udpTransport = new UdpTransport.Builder().build()
val reporter = 
    ...
    .withTransport(udpTransport)
    ...
~~~

### Dropwizard Metrics Reporter

If you have a dropwizard project and have at least `dropwizard-core` 0.7.X, 
then you can perform the following steps to automatically report metrics to
datadog.

First, add the `dropwizard-metrics-datadog` dependency in your POM:

~~~xml    
    <dependency>
        <groupId>org.coursera</groupId>
        <artifactId>dropwizard-metrics-datadog</artifactId>
        <version>1.0.1</version>
    </dependency>
~~~

Then just add the following to your `dropwizard` YAML config file.

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: datadog
      host: <host>                          # Optional with UDP Transport
      tags:                                 # Optional. Defaults to (empty)
      includes:                             # Optional. Defaults to (all).
      excludes:                             # Optional. Defaults to (none).
      http:
        apiKey: <apiKey>
        connectTimeout: <duration>          # Optional. Default is 5 seconds
        socketTimeout: <duration>           # Optional. Default is 5 seconds
~~~

Once your `dropwizard` application starts, your metrics should start appearing
in Datadog.

#### Transport options

HTTP Transport:

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: datadog
      host: <host>
      transport:
        type: http
        apiKey: <apiKey>
        connectTimeout: <duration>          # Optional. Default is 5 seconds
        socketTimeout: <duration>           # Optional. Default is 5 seconds
~~~

UDP Transport:

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: datadog
      transport:
        type: udp
        prefix:                             # Optional. Default is (empty)
        statsdHost: "localhost"             # Optional. Default is "localhost"
        port: 8125                          # Optional. Default is 8125
~~~

#### Filtering

If you want to filter only a few metrics, you can use the `includes` or 
`excludes` key to create a set of metrics to include or exclude respectively.

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: datadog
      host: <host>
      includes:
        - jvm.
        - ch.
~~~

The check is very simplistic so be as specific as possible. For example, if 
you have "jvm.", the filter will check if the includes has that value in any 
part of the metric name (not just the beginning).

## Maven Info

Metrics datadog reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.coursera%22%20AND%20a%3A%22metrics-datadog%22)

* Group: org.coursera
* Artifact: metrics-datadog
* Version: 1.0.1

Dropwizard datadog reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.coursera%22%20AND%20a%3A%22dropwizard-metrics-datadog%22)

* Group: org.coursera
* Artifact: dropwizard-metrics-datadog
* Version: 1.0.1

## Contributing

We follow Google's [Java Code
Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html)
