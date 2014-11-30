package org.fc.bookmixer.services;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import org.fc.bookmixer.support.NLPUtils;
import org.fc.bookmixer.support.NameType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NamedEntitiesService {

  private static final String CLASSIFIER_MODEL = "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz";

  private static AbstractSequenceClassifier<CoreLabel> classifier;

  static {
    try {
      classifier = CRFClassifier.getClassifier(CLASSIFIER_MODEL);
    } catch (Exception e) {}
  }

  public Map<String, Integer> getEntities(String filePath, NameType type) {
    return namesAsMap(analyzeForType(filePath, type.name()));
  }

  public String annotate(String filePath) {
    StringBuilder sb = new StringBuilder();
    List<List<CoreLabel>> out = classifier.classifyFile(filePath);
    for (List<CoreLabel> sentence : out) {
      for (CoreLabel word : sentence) {
        sb.append(word.word());
        String annotation = word.get(CoreAnnotations.AnswerAnnotation.class);
        if (!"O".equals(annotation)) {
          sb.append("[" + annotation + "]");
        }
        sb.append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private static Multiset<String> analyzeForType(String filePath, String type) {
    final Multiset<String> names = HashMultiset.create();
    List<List<CoreLabel>> out = classifier.classifyFile(filePath);
    for (List<CoreLabel> sentence : out) {
      String previousName = null;
      String currentName = null;
      for (CoreLabel word : sentence) {
        if (type.equals(word.get(CoreAnnotations.AnswerAnnotation.class)) && word.word().length() > 2) {
          currentName = previousName == null ? word.word() : previousName + " " + word.word();
          previousName = currentName;
        } else {
          if (currentName != null) {
            names.add(currentName);
          }
          previousName = null;
          currentName = null;
        }
      }
    }
    mergeSingleNamesIntoCompositeNames(names);
    return names;
  }

  private static String pickBestCompositeName(String currentName, Multiset<String> names) {
    String bestPossibleCompositeName = "";
    int bestPossibleCompositeNameCount = 0;
    for (String possibleCompositeName : names) {
      if (possibleCompositeName.indexOf(' ') != -1 && possibleCompositeName.contains(currentName)) {
        if (names.count(possibleCompositeName) > bestPossibleCompositeNameCount && names.count(possibleCompositeName) > 1) {
          bestPossibleCompositeNameCount = names.count(possibleCompositeName);
          bestPossibleCompositeName = possibleCompositeName;
        }
      }
    }
    return bestPossibleCompositeName;

  }

  private static Map<String, Integer> namesAsMap(Multiset<String> names) {
    Map<String, Integer> namesWithCount = Maps.newTreeMap();
    for (String name : names) {
      namesWithCount.put(name, names.count(name));
    }
    return NLPUtils.sortByCount(namesWithCount);
  }

  private static void mergeSingleNamesIntoCompositeNames(Multiset<String> names) {
    for (Iterator<String> nameIterator = names.elementSet().iterator(); nameIterator.hasNext();) {
      String name = nameIterator.next();
      if (name.indexOf(' ') == -1) {
        // single name : we check if a matching composite name exists
        String bestPossibleCompositeName = pickBestCompositeName(name, names);
        if (!bestPossibleCompositeName.equals("")) {
          //System.out.println("Adding for term " + bestPossibleCompositeName + " : existing count (" + names.count(bestPossibleCompositeName) + ") + new count for term " + name + " (" + names.count(name) + ")");
          names.setCount(bestPossibleCompositeName, names.count(bestPossibleCompositeName) + names.count(name));
          nameIterator.remove();
        }
      }
    }
  }
}
