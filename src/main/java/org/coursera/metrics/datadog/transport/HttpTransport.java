package org.coursera.metrics.datadog.transport;

import org.coursera.metrics.datadog.model.DatadogCounter;
import org.coursera.metrics.datadog.model.DatadogGauge;
import org.coursera.metrics.serializer.JsonSerializer;
import org.coursera.metrics.serializer.Serializer;
import org.apache.http.entity.ContentType;

import java.io.IOException;

import static org.apache.http.client.fluent.Request.*;

/**
 * Uses the datadog http webservice to push metrics.
 *
 * @see <a href="http://docs.datadoghq.com/api/">API docs</a>
 */
public class HttpTransport implements Transport {

  private final static String BASE_URL = "https://app.datadoghq.com/api/v1";
  private final String seriesUrl;
  private final int connectTimeout;     // in milliseconds
  private final int socketTimeout;      // in milliseconds

  private HttpTransport(String apiKey, int connectTimeout, int socketTimeout) {
    this.seriesUrl = String.format("%s/series?api_key=%s", BASE_URL, apiKey);
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
  }

  public static class Builder {
    String apiKey;
    int connectTimeout = 5000;
    int socketTimeout = 5000;


    public Builder withApiKey(String key) {
      this.apiKey = key;
      return this;
    }

    public Builder withConnectTimeout(int milliseconds) {
      this.connectTimeout = milliseconds;
      return this;
    }

    public Builder withSocketTimeout(int milliseconds) {
      this.socketTimeout = milliseconds;
      return this;
    }

    public HttpTransport build() {
      return new HttpTransport(apiKey, connectTimeout, socketTimeout);
    }
  }

  public Request prepare() throws IOException {
    return new HttpRequest(this);
  }

  public void close() throws IOException {
  }

  public static class HttpRequest implements Transport.Request {
    protected final Serializer serializer;

    protected final HttpTransport transport;

    public HttpRequest(HttpTransport transport) throws IOException {
      this.transport = transport;
      serializer = new JsonSerializer();
      serializer.startObject();
    }

    public void addGauge(DatadogGauge gauge) throws IOException {
      serializer.appendGauge(gauge);
    }

    public void addCounter(DatadogCounter counter) throws IOException {
      serializer.appendCounter(counter);
    }

    public void send() throws Exception {
      serializer.endObject();
      Post(this.transport.seriesUrl)
          .useExpectContinue()
          .connectTimeout(this.transport.connectTimeout)
          .socketTimeout(this.transport.socketTimeout)
          .bodyString(serializer.getAsString(), ContentType.APPLICATION_JSON)
          .execute()
          .discardContent();
    }
  }
}
