package com.yammer.metrics.reporting;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpTransport implements Transport {
    private final String seriesUrl;

    public HttpTransport(String host, String apiKey) {
        this.seriesUrl = String.format("https://%s/api/v1/series?api_key=%s", host, apiKey);
    }

    public Request prepare() throws IOException {
        return new HttpRequest(this);
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

            org.apache.http.client.fluent.Request.Post(this.transport.seriesUrl)
                    .addHeader("Content-Type", "application/json")
                    .bodyByteArray(this.out.toByteArray())
                    .execute();
        }
    }
}
