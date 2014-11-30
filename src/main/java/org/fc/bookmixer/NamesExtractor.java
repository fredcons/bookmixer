package org.fc.bookmixer;

import org.fc.bookmixer.services.NamedEntitiesService;
import org.fc.bookmixer.support.NLPUtils;
import org.fc.bookmixer.support.NameType;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Map;

public class NamesExtractor {

  private Map<String, Integer> people = null;
  private Map<String, Integer> locations = null;

  private NamedEntitiesService namedEntitiesService = null;

  @Option(name = "--file", required = true, usage = "Path of the file to analyse")
  protected String targetFilePath;

  public static void main(String[] args) throws Exception {
    NamesExtractor namesExtractor = new NamesExtractor();
    namesExtractor.doMain(args);
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
    extract();
  }

  protected void extract() throws Exception {

    System.out.println("Analyzing "  + targetFilePath);

    people = namedEntitiesService.getEntities(targetFilePath, NameType.PERSON);
    NLPUtils.displayNames("Persons", people);

    locations = namedEntitiesService.getEntities(targetFilePath, NameType.LOCATION);
    NLPUtils.displayNames("Locations", locations);

  }

}
