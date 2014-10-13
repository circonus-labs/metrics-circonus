package org.coursera.metrics.datadog.transport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.coursera.metrics.datadog.model.DatadogCounter;
import org.coursera.metrics.datadog.model.DatadogGauge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.apache.http.client.fluent.Request.*;

public class HttpTransport implements Transport {

  private final static String BASE_URL = "https://app.datadoghq.com/api/v1";
  private final String seriesUrl;
  private final int connectTimeout;     // in milliseconds
  private final int socketTimeout;      // in milliseconds

  public HttpTransport(String apiKey, int connectTimeout, int socketTimeout) {
    this.seriesUrl = String.format("%s/series?api_key=%s", BASE_URL, apiKey);
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
  }

  public Request prepare() throws IOException {
    return new HttpRequest(this);
  }

  public static class HttpRequest implements Transport.Request {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper(JSON_FACTORY);
    private final HttpTransport transport;
    private final JsonGenerator jsonOut;
    private final ByteArrayOutputStream out;

    HttpRequest(HttpTransport transport)
        throws IOException {
      this.transport = transport;
      this.out = new ByteArrayOutputStream(4096);
      this.jsonOut = JSON_FACTORY.createGenerator(out);
      jsonOut.writeStartObject();
      jsonOut.writeArrayFieldStart("series");
    }

    private void add(Object value) throws IOException {
      MAPPER.writeValue(jsonOut, value);
    }

    public void addGauge(String name,
                         Number value,
                         long timestamp,
                         String host,
                         List<String> additionalTags)
          throws IOException {
      add(new DatadogGauge(name, value, timestamp, host, additionalTags));
    }

    public void addCounter(String name,
                           Long value,
                           long timestamp,
                           String host,
                           List<String> additionalTags)
        throws IOException {
      add(new DatadogCounter(name, value, timestamp, host, additionalTags));
    }

    public void send() throws Exception {
      jsonOut.writeEndArray();
      jsonOut.writeEndObject();
      jsonOut.flush();
      out.flush();
      out.close();
      Post(this.transport.seriesUrl)
          .useExpectContinue()
          .connectTimeout(this.transport.connectTimeout)
          .socketTimeout(this.transport.socketTimeout)
          .bodyString(out.toString("UTF-8"), ContentType.APPLICATION_JSON)
          .execute()
          .discardContent();
    }
  }
}
