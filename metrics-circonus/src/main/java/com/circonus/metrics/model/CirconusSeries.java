package com.circonus.metrics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.circonus.metrics.TaggedName;

public abstract class CirconusSeries<T extends Number> {
  private String name;
  private T count;
  private Long epoch;
  private String host;
  private List<String> tags;

  public CirconusSeries(String name, T count, Long epoch, String host, List<String> additionalTags) {
    TaggedName taggedName = TaggedName.decode(name);
    this.name = taggedName.getMetricName();
    this.tags = taggedName.getEncodedTags();

    if (additionalTags != null) {
      this.tags.addAll(additionalTags);
    }
    this.count = count;
    this.epoch = epoch;
    this.host = host;
  }

  public String metric() {
    return name;
  }

  @JsonInclude(Include.NON_NULL)
  public List<String> get_tags() {
    return tags;
  }
  public Number get_value() {
    return count;
  }
  public String get_type() {
    return "n";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CirconusSeries)) return false;

    CirconusSeries that = (CirconusSeries) o;

    if (!count.equals(that.count)) return false;
    if (!epoch.equals(that.epoch)) return false;
    if (!host.equals(that.host)) return false;
    if (!name.equals(that.name)) return false;
    if (!tags.equals(that.tags)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + count.hashCode();
    result = 31 * result + epoch.hashCode();
    result = 31 * result + host.hashCode();
    result = 31 * result + tags.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "CirconusSeries{" +
        "name='" + name + '\'' +
        ", count=" + count +
        ", epoch=" + epoch +
        ", host='" + host + '\'' +
        ", tags=" + tags +
        '}';
  }
}
