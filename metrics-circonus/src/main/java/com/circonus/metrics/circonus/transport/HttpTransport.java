package com.circonus.metrics.circonus.transport;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.*;
import org.apache.http.conn.socket.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import com.circonus.metrics.circonus.model.CirconusCounter;
import com.circonus.metrics.circonus.model.CirconusGauge;
import com.circonus.metrics.circonus.model.CirconusHistogram;
import com.circonus.metrics.serializer.JsonSerializer;
import com.circonus.metrics.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.client.fluent.Request.Post;

/**
 * Uses the circonus HTTPTrap to push metrics.
 *
 * @see <a href="https://login.circonus.com/user/docs/Data/CheckTypes#HTTPTrap">HTTPTrap docs</a>
 */
public class HttpTransport implements Transport {

  private static final Logger LOG = LoggerFactory.getLogger(HttpTransport.class);

  private final String endpoint;
  private final String apiKey;
  private final int connectTimeout;     // in milliseconds
  private final int socketTimeout;      // in milliseconds
  private final Executor executor;
  private final HttpHost proxy;

  private HttpTransport(String protocol, String host, String checkId, String checkSecret,
                        String apiKey, int connectTimeout, int socketTimeout, HttpHost proxy) {
    this.endpoint = String.format("%s://%s/module/httptrap/%s/%s", protocol, host, checkId, checkSecret);
    this.apiKey = apiKey;
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
    this.proxy = proxy;
    HttpClient client = null;

    /* Circonus has it's own CA (for brokers), so if the host isn't a circonus.com address, we need
     * to disable all the SSL checking (it won't have a cert that can be validated with a normal
     * chain, not will the hostname match.
     */
    if(!host.matches("\\.circonus\\.com(?::\\d+)?")) {
      try {
        HttpClientBuilder b = HttpClientBuilder.create();

        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            return true;
          }
        }).build();

        TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
          }
        };

        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        b.setSslcontext( sslContext);

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
          .register("http", PlainConnectionSocketFactory.getSocketFactory())
          .register("https", sslSocketFactory)
          .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
        b.setConnectionManager( connMgr);

        client = b.build();
      } catch(Exception e) {
        LOG.error("SSL bypass failed", e);
      }
    }
    this.executor = (client == null) ? null : Executor.newInstance(client);
  }

  private Response execute(org.apache.http.client.fluent.Request request) throws ClientProtocolException, IOException {
    if (this.proxy != null)
      request.viaProxy(this.proxy);

    if(this.executor == null) return request.execute();
    return this.executor.execute(request);
  }

  public static class Builder {
    String apiKey;
    String checkId;
    String checkSecret;
    String host = "trap.noit.circonus.net";
    String protocol = "https";
    int connectTimeout = 5000;
    int socketTimeout = 5000;
    HttpHost proxy;

    public Builder withApiKey(String key) {
      this.apiKey = key;
      return this;
    }

    public Builder withCheckId(String key) {
      this.checkId = key;
      return this;
    }

    public Builder withCheckSecret(String key) {
      this.checkSecret = key;
      return this;
    }

    public Builder withBroker(String key) {
      this.host = key;
      return this;
    }

    public Builder withProtocol(String key) {
      this.protocol = key;
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

    public Builder withProxy(String proxyHost, int proxyPort) {
      this.proxy = new HttpHost(proxyHost, proxyPort);
      return this;
    }

    public HttpTransport build() {
      return new HttpTransport(protocol, host, checkId, checkSecret, apiKey, connectTimeout, socketTimeout, proxy);
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

    public void addGauge(CirconusGauge gauge) throws IOException {
      serializer.appendGauge(gauge);
    }

    public void addCounter(CirconusCounter counter) throws IOException {
      serializer.appendCounter(counter);
    }

    public void addHistogram(CirconusHistogram hist) throws IOException {
      serializer.appendHistogram(hist);
    }

    public void send() throws Exception {
      serializer.endObject();
      String postBody = serializer.getAsString();
      if (LOG.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sending HTTP POST request to ");
        sb.append(this.transport.endpoint);
        sb.append(", POST body is: \n");
        sb.append(postBody);
        LOG.debug(sb.toString());
      }
      long start = System.currentTimeMillis();
      org.apache.http.client.fluent.Request request = Post(this.transport.endpoint)
        .useExpectContinue()
        .connectTimeout(this.transport.connectTimeout)
        .socketTimeout(this.transport.socketTimeout)
        .setHeader("X-Circonus-Auth-Token", this.transport.apiKey)
        .setHeader("X-Circonus-App-Name", "com.circonus.cip")
        .bodyString(postBody, ContentType.APPLICATION_JSON);

      Response response = this.transport.execute(request);

      long elapsed = System.currentTimeMillis() - start;

      if (LOG.isDebugEnabled()) {
        HttpResponse httpResponse = response.returnResponse();
        StringBuilder sb = new StringBuilder();

        sb.append("Sent metrics to Circonus: ");
        sb.append("  Timing: ").append(elapsed).append(" ms\n");
        sb.append("  Status: ").append(httpResponse.getStatusLine().getStatusCode()).append("\n");

        String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        sb.append("  Content: ").append(content);

        LOG.debug(sb.toString());
      } else {
        response.discardContent();
      }
    }
  }
}
