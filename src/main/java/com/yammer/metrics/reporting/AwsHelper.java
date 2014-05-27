package com.yammer.metrics.reporting;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AwsHelper {

  public static final String url = "http://169.254.169.254/latest/meta-data/instance-id";

  public static String getEc2InstanceId() throws IOException {
      CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();;
    try {
      final HttpResponse response = client.execute(new HttpGet(url), null).get();
      return EntityUtils.toString(response.getEntity());
    } catch (Throwable t) {
      throw new IOException(t);
    }
  }
}