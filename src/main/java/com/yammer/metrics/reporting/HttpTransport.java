package com.yammer.metrics.reporting;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;

public class HttpTransport implements Transport {
    private final CloseableHttpAsyncClient client;
    private final String seriesUrl;

    public HttpTransport(String host, String apiKey) {
        this.client = HttpAsyncClients.createDefault();
        this.seriesUrl = String.format("https://%s/api/v1/series?api_key=%s", host, apiKey);
    }

    public Request prepare() throws IOException {
        return new HttpRequest(this);
    }

    public Future<HttpResponse> execute(HttpUriRequest request) throws Exception {
        return this.client.execute(request, new FutureCallback<HttpResponse>() {
            public void completed(HttpResponse result) {
                LOG.debug("Completed sending metrics to datadog");
            }

            public void failed(Exception ex) {
                LOG.error("Error Writing Datadog metrics", ex);
            }

            public void cancelled() {
                LOG.debug("Cancelled request to send metrics to datadog");
            }
        });
    }

    public static class HttpRequest implements Transport.Request {
        private final HttpTransport transport;
        private final HttpPost request;
        private final ByteArrayOutputStream out;

        public HttpRequest(HttpTransport transport) throws IOException {
            this.transport = transport;
            this.request = new HttpPost(this.transport.seriesUrl);
            this.out = new ByteArrayOutputStream();
        }

        public OutputStream getBodyWriter() {
            return out;
        }

        public void send() throws Exception {
            this.out.flush();
            this.out.close();
            this.request.setEntity(new ByteArrayEntity(out.toByteArray(), ContentType.APPLICATION_JSON));
            this.transport.execute(this.request).get();
        }
    }
}
