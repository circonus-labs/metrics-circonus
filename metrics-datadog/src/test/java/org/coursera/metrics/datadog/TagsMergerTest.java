package org.coursera.metrics.datadog;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TagsMergerTest {

  @Test
  public void testMergeTags() throws Exception {
    List<String> tags1 = new ArrayList<>();
    tags1.add("key1:v1");
    tags1.add("key2:v2");
    List<String> tags2 = new ArrayList<>();
    tags2.add("key3:v3");
    tags2.add("key4:v4");


    List<String> expected = new ArrayList<>();
    expected.addAll(tags1);
    expected.addAll(tags2);
    assert(TagsMerger.mergeTags(tags1, tags2).equals(expected));
  }
}