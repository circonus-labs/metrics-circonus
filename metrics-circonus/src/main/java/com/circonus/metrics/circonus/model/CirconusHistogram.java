package com.circonus.metrics.circonus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.circonus.metrics.circonus.TaggedName;
import com.circonus.metrics.circonus.HistImpl;

public class CirconusHistogram {
  private String name;
  private HistImpl hist;
  private Long epoch;
  private String host;
  private List<String> tags;

  public CirconusHistogram(String name, HistImpl hist, Long epoch, String host, List<String> additionalTags) {
    TaggedName taggedName = TaggedName.decode(name);
    this.name = taggedName.getMetricName();
    this.tags = taggedName.getEncodedTags();

    if (additionalTags != null) {
      this.tags.addAll(additionalTags);
    }
    this.hist = hist;
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
  public String[] get_value() {
    return hist.toDecStrings();
  }
  public String get_type() {
    return "n";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CirconusHistogram)) return false;

    CirconusHistogram that = (CirconusHistogram) o;

    if (!hist.equals(that.hist)) return false;
    if (!epoch.equals(that.epoch)) return false;
    if (!host.equals(that.host)) return false;
    if (!name.equals(that.name)) return false;
    if (!tags.equals(that.tags)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + hist.hashCode();
    result = 31 * result + epoch.hashCode();
    result = 31 * result + host.hashCode();
    result = 31 * result + tags.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "CirconusHist{" +
        "name='" + name + '\'' +
        ", hist=..." +
        ", epoch=" + epoch +
        ", host='" + host + '\'' +
        ", tags=" + tags +
        '}';
  }
}
