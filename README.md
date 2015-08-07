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

### Tag encoding and expansion

Datadog supports powerful [tagging](http://docs.datadoghq.com/faq/#tagging) 
functionality while the Metrics API does not. Thus, `metrics-datadog` utilizes 
a special, overloaded metric naming syntax that enables tags to piggyback on
metric names while passing through the Metrics library. The tags are unpacked 
by `metrics-datadog` at reporting time and are sent along to Datadog via the
configured transport layer. Here's the metric name syntax:

`[tagName:tagValue,tagName:tagValue,...]`

`metrics-datadog` is mainly a reporting library and doesn't currently 
implement a tag-aware decorator on top of the core `Metrics` API. It
does, however, expose a `TaggedName` class that helps you encode/decode tags in 
metric names using the syntax above. You can utilize this helper class
methods when registering and recording metrics. Note that in order for tag
propagation to work, you'll need to use our `DefaultMetricNameFormatter` 
(or a formatter with compatible parsing logic).

We also support the notion of static, "additional tags". This feature allows 
you to define a set of tags that are appended to all metrics sent through 
the reporter. It's useful for setting static tags such as the 
environment, service name or version. Additional tags are configured via 
the `DatadogReporter` constructor. 

Finally, we support the notion of "dynamic tags". By implementing and 
registering a `DynamicTagsCallback` with `DatadogReporter`, you can control
the values of "additional tags" at runtime. Dynamic tags are merged with 
and override any additional tags set.

*Performance note*: Heavy use of tagging, especially tags values with high 
cardinality, can dramatically increase memory usage, as all tag permutations
are tracked and counted in-memory by the Metrics library. Also note that some
[MetricRegistry APIs](https://github.com/dropwizard/metrics/blob/master/metrics-core/src/main/java/io/dropwizard/metrics/MetricRegistry.java#L376)
do defensive copies on the entire metrics set, which can be prohibitively 
expensive CPU and memory-wise if you have a huge, heavily tagged metric set.

### Dropwizard Metrics Reporter

If you have a dropwizard project and have at least `dropwizard-core` 0.7.X, 
then you can perform the following steps to automatically report metrics to
datadog.

First, add the `dropwizard-metrics-datadog` dependency in your POM:

~~~xml    
    <dependency>
        <groupId>org.coursera</groupId>
        <artifactId>dropwizard-metrics-datadog</artifactId>
        <version>1.0.2</version>
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
      transport:
        type: http
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
* Version: 1.0.2

Dropwizard datadog reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.coursera%22%20AND%20a%3A%22dropwizard-metrics-datadog%22)

* Group: org.coursera
* Artifact: dropwizard-metrics-datadog
* Version: 1.0.2

## Contributing

We follow Google's [Java Code
Style](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html)
