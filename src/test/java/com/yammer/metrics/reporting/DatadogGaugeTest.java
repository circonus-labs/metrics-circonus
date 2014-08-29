package com.yammer.metrics.reporting;

import com.yammer.metrics.reporting.model.DatadogGauge;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DatadogGaugeTest {

  @Test
  public void testSplitNameAndTags() {
    DatadogGauge gauge = new DatadogGauge(
        "test[tag1:value1,tag2:value2,tag3:value3]", 1L, 1234L, "Test Host");
    List<String> tags = gauge.getTags();

    assertEquals(3, tags.size());
    assertEquals("tag1:value1", tags.get(0));
    assertEquals("tag2:value2", tags.get(1));
    assertEquals("tag3:value3", tags.get(2));
  }
}
