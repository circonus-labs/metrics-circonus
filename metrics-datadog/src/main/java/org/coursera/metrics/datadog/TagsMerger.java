package org.coursera.metrics.datadog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TagsMerger {
  private static final Logger LOG = LoggerFactory.getLogger(TagsMerger.class);

  /**
   *
   * @param tags1 list of tags, each tag should be in the format of "key:value"
   * @param tags2 list of tags, each tag should be in the format of "key:value"
   * @return merged tags list. If there is duplicated key, tags in tags2 will overwrite tags
   * in tags1, and tags in the back of the list will overwrite tags in the front of the list.
   */
  static List<String> mergeTags(List<String> tags1, List<String> tags2) {
    List<String> newTags = new ArrayList<String>();
    newTags.addAll(tags1);
    newTags.addAll(tags2);

    Map<String, String> map = new HashMap<String, String>();
    for (String tag : newTags) {
      String[] strs = tag.split(":");
      if (strs.length != 2) {
        LOG.warn("Invalid tag: " + tag);
      } else {
        map.put(strs[0], strs[1]);
      }
    }

    newTags.clear();
    for (Map.Entry entry : map.entrySet()) {
      newTags.add(entry.getKey() + ":" + entry.getValue());
    }

    return newTags;
  }
}
