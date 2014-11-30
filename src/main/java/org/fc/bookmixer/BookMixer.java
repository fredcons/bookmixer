package org.fc.bookmixer;

import com.google.common.collect.Maps;
import edu.stanford.nlp.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.fc.bookmixer.services.NamedEntitiesService;
import org.fc.bookmixer.support.NLPUtils;
import org.fc.bookmixer.support.NameType;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.*;

public class BookMixer {

  private Map<String, Integer> newPeople = null;
  private Map<String, Integer> newLocations = null;

  private Map<String, Integer> existingPeople = null;
  private Map<String, Integer> existingLocations = null;

  private NamedEntitiesService namedEntitiesService = null;

  @Option(name="--story", required = true, usage = "Path of the file containing the story you want to use")
  protected String storyFilePath;
  @Option(name="--names", required = true, usage = "Path of the file containing the pepple and locations you want to use")
  protected String namesFilePath;
  @Option(name="--output", required = true, usage = "Path of the folder used for result generation")
  protected String targetFilePath;
  @Option(name="--randompeople", usage = "Whether to use random people or people having the same relative rank. Default is relative ranking")
  protected boolean randomPeople = false;
  @Option(name="--randomlocations", usage = "Whether to use random locations or locations having the same relative rank.Default is relative ranking")
  protected boolean randomLocations = false;
  @Option(name="--keeppeople", usage = "Whether to keep the characters or to swap them. Default is to swap them")
  protected boolean keepPeople = false;
  @Option(name="--keeplocations", usage = "Whether to keep the locations or to swap them. Default is to swap them")
  protected boolean keepLocations = false;

  public static void main(String[] args) throws Exception {
    BookMixer bookMixer = new BookMixer();
    bookMixer.doMain(args);
  }

  protected void doMain(String[] args) throws Exception {

    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.out.println(e.getMessage());
      parser.printUsage(System.out);
      return;
    }
    namedEntitiesService = new NamedEntitiesService();
    storify();

  }

  protected void storify() throws Exception {

    System.out.println("Analyzing "  + namesFilePath);
    String annotatedNamesFile = namedEntitiesService.annotate(namesFilePath);
    NLPUtils.writeContent(new File(targetFilePath, "annotated_names.txt"), annotatedNamesFile);

    System.out.println("Parsing new names");
    newPeople = namedEntitiesService.getEntities(namesFilePath, NameType.PERSON);
    NLPUtils.writeNames(new File(targetFilePath, "new_people.txt"), newPeople);

    System.out.println("Parsing new locations");
    newLocations = namedEntitiesService.getEntities(namesFilePath, NameType.LOCATION);
    newLocations = filter(newLocations, newPeople);
    NLPUtils.writeNames(new File(targetFilePath, "new_locations.txt"), newLocations);

    System.out.println("Analyzing "  + storyFilePath);
    String annotatedStoryFile = namedEntitiesService.annotate(namesFilePath);
    NLPUtils.writeContent(new File(targetFilePath, "annotated_story.txt"), annotatedStoryFile);

    System.out.println("Parsing existing names");
    existingPeople = namedEntitiesService.getEntities(storyFilePath, NameType.PERSON);

    System.out.println("Parsing existing locations");
    existingLocations = namedEntitiesService.getEntities(storyFilePath, NameType.LOCATION);

    existingLocations = filter(existingLocations, existingPeople);

    NLPUtils.writeNames(new File(targetFilePath, "existing_people.txt"), existingPeople);
    NLPUtils.writeNames(new File(targetFilePath, "existing_locations.txt"), existingLocations);

    String existingText = IOUtils.slurpFile(storyFilePath);
    if (!keepPeople) {
      existingText = swap(existingText, existingPeople, newPeople, randomPeople);
    }
    if (!keepLocations) {
      existingText = swap(existingText, existingLocations, newLocations, randomLocations);
    }
    FileUtils.writeStringToFile(new File(targetFilePath, "resulting_story.txt"), existingText.toString());

  }

  private String swap(String text, Map<String, Integer> existingNames, Map<String, Integer> newNames, boolean randomMode) throws Exception {
    Map<String, String> namesMapping = Maps.newHashMap();
    for (String existingName : existingNames.keySet()) {
      String newName = namesMapping.get(existingName);
      if (newName == null) {
        newName = randomMode ? pickRandomKey(newNames.keySet()) : pickSimilarKey(existingName, existingNames, newNames);
        namesMapping.put(existingName, newName);
      }
      text = text.replace(existingName, newName);
    }
    return text;
  }

  private Map<String, Integer> filter(Map<String, Integer> toBeFiltered, Map<String, Integer> toBeKept) {
    Map<String, Integer> result = Maps.newHashMap(toBeFiltered);
    for (Iterator<String> iterator = result.keySet().iterator(); iterator.hasNext();) {
      String key = iterator.next();
      if (toBeKept.containsKey(key)) {
        iterator.remove();
      }
    }
    return NLPUtils.sortByCount(result);
  }

  private static String pickRandomKey(Set<String> set) {
    Random random = new Random();
    List<String> keys  = new ArrayList<String>(set);
    return keys.get(random.nextInt(keys.size()));
  }

  private static String pickSimilarKey(String targetName, Map<String, Integer> existingNames, Map<String, Integer> newNames) {
    int existingNameRank = 0;
    for (String existingName : existingNames.keySet()) {
      existingNameRank++;
      if (existingName.equals(targetName)) break;
    }
    if (existingNameRank > newNames.size()) {
      throw new IllegalArgumentException("Not enough values in new names map");
    }
    int newNameRank = 0;
    for (String newName : newNames.keySet()) {
      newNameRank++;
      if (newNameRank == existingNameRank) {
        return newName;
      }
    }
    return "";
  }
}
