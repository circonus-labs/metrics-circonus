# Metrics Circonus Reporter

Simple Metrics reporter that sends metrics reporting info to Circonus via HTTPTrap.

## Usage

~~~java
import com.circonus.metrics.circonus.CirconusReporter
import com.circonus.metrics.circonus.transport.HttpTransport

...
HttpTransport httpTransport = new HttpTransport.Builder()
  .withApiKey(apiKey)  // API token from Circonus
  .withCheckId(checkUuid) // Check UUID of the HTTPTrap
  .withCheckSecret(checkSecret) // Secret for the HTTPTrap
  // .withProtocol("https") // "https" is the default
  // .withBroker("172.16.99.13:43191") // My enterprise broker
  .build()
CirconuReporter reporter = CirconuReporter.forRegistry(registry)
  .withEC2Host()
  .withTransport(httpTransport)
  .build()

reporter.start(10, TimeUnit.SECONDS)
~~~

### Tag encoding and expansion

`metrics-circonus` utilizes a special, overloaded metric naming syntax that
enables tags to piggyback on metric names while passing through the Metrics
library. The tags are unpacked by `metrics-circonus` at reporting time and
are sent along to Circonus via the transport layer. Here's the metric name syntax:

`[tagCategory:tag,tagCategory:tag,...]`

`metrics-circonus` is mainly a reporting library and doesn't currently 
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
the `CirconusReporter` constructor. 

Finally, we support the notion of "dynamic tags". By implementing and 
registering a `DynamicTagsCallback` with `CirconusReporter`, you can control
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
Circonus.

First, add the `dropwizard-metrics-circonus` dependency in your POM:

~~~xml    
    <dependency>
        <groupId>com.circonus</groupId>
        <artifactId>dropwizard-metrics-circonus</artifactId>
        <version>1.1.2</version>
    </dependency>
~~~

Then just add the following to your `dropwizard` YAML config file.

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: circonus
      host: <host>                          # Optional with UDP Transport
      tags:                                 # Optional. Defaults to (empty)
      includes:                             # Optional. Defaults to (all).
      excludes:                             # Optional. Defaults to (none).
      transport:
        type: http
        apiKey: <apiKey>
        checkId: <checkUuid>
        checkSecret: <checkSecret>
        protocol: https                     # Optional. Default is https
        broker: "host:port"                 # Optional. Default is trap.noit.circonus.net:443
        connectTimeout: <duration>          # Optional. Default is 5 seconds
        socketTimeout: <duration>           # Optional. Default is 5 seconds
~~~

Once your `dropwizard` application starts, your metrics should start appearing
in Circonus.

#### Filtering

If you want to filter only a few metrics, you can use the `includes` or 
`excludes` key to create a set of metrics to include or exclude respectively.

~~~yaml
metrics:
  frequency: 1 minute                       # Default is 1 second.
  reporters:
    - type: circonus
      host: <host>
      includes:
        - jvm.
        - ch.
~~~

The check is very simplistic so be as specific as possible. For example, if 
you have "jvm.", the filter will check if the includes has that value in any 
part of the metric name (not just the beginning).

## Maven Info

Metrics circonus reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.circonus%22%20AND%20a%3A%22metrics-circonus%22)

* Group: com.circonus
* Artifact: metrics-circonus
* Version: 1.0.0

Dropwizard circonus reporter is available as an artifact on
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.circonus%22%20AND%20a%3A%22dropwizard-metrics-circonus%22)

* Group: com.circonus
* Artifact: dropwizard-metrics-circonus
* Version: 1.0.0

## Origins

This code is based on [work done a Coursera](https://github.com/coursera/metrics-datadog)
by Daniel Chia and Nick Dellamaggiore.  Thanks!

## Contributing

Send a pull request!
