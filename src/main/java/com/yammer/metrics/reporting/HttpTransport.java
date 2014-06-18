package com.yammer.metrics.reporting;

import org.apache.http.entity.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpTransport implements Transport {
    private final String seriesUrl;

    public HttpTransport(String apiKey) {
        this.seriesUrl = String.format("https://app.datadoghq.com/api/v1/series?api_key=%s", apiKey);
    }

    public Request prepare() throws IOException {
        return new HttpRequest(this);
    }

    public static class HttpRequest implements Transport.Request {
        private final HttpTransport transport;
        private final ByteArrayOutputStream out;

        public HttpRequest(HttpTransport transport) throws IOException {
            this.transport = transport;
            this.out = new ByteArrayOutputStream();
        }

        public OutputStream getBodyWriter() {
            return out;
        }

        public void send() throws Exception {
            this.out.flush();
            this.out.close();

            org.apache.http.client.fluent.Request.Post(this.transport.seriesUrl)
                    .addHeader("Content-Type",
                               ContentType.APPLICATION_JSON.toString())
                    .bodyByteArray(this.out.toByteArray())
                    .execute();
        }
    }
}
