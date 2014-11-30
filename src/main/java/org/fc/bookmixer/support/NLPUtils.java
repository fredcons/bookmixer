package org.fc.bookmixer.support;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;

public class NLPUtils {

  public static void displayNames(String header, Map<String, Integer> names) {
    System.out.println(header + " : ");
    for (Map.Entry<String, Integer> nameWithCount : names.entrySet()) {
      System.out.println(nameWithCount.getKey() + " : " + nameWithCount.getValue());
    }
  }

  public static void writeNames(File targetFile, Map<String, Integer> names) throws Exception {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Integer> nameWithCount : names.entrySet()) {
      sb.append(nameWithCount.getKey()).append(" : ").append(nameWithCount.getValue()).append("\n");
    }
    FileUtils.write(targetFile, sb.toString());
  }

  public static void writeContent(File targetFile, String content) throws Exception {
    FileUtils.write(targetFile, content);
  }

  public static Map<String, Integer> sortByCount(Map<String, Integer> map) {
    Ordering<String> naturalReverseValueOrdering = Ordering.natural().reverse().nullsLast().onResultOf(Functions.forMap(map, null)).compound(Ordering.natural());
    return ImmutableSortedMap.copyOf(map, naturalReverseValueOrdering);
  }

}
