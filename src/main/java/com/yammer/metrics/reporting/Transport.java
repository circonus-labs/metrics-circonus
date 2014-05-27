package com.yammer.metrics.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public interface Transport {

  static final Logger LOG = LoggerFactory.getLogger(Transport.class);

  public Request prepare() throws IOException;

  public interface Request {
    OutputStream getBodyWriter();
    void send() throws Exception;
  }
}
