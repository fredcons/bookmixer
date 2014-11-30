package org.fc.bookmixer;

import org.fc.bookmixer.services.NamedEntitiesService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class TextAnnotator {

  private NamedEntitiesService namedEntitiesService = null;

  @Option(name = "--file", required = true, usage = "Path of the file to analyze")
  protected String targetFilePath;

  public static void main(String[] args) throws Exception {
    TextAnnotator textAnnotator = new TextAnnotator();
    textAnnotator.doMain(args);
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
    annotate();
  }

  protected void annotate() throws Exception {

    System.out.println("Analyzing "  + targetFilePath);

    String annotatedContent = namedEntitiesService.annotate(targetFilePath);
    System.out.println(annotatedContent);

  }

}
