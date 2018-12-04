package graph;

import config.Path;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HashSolver {

  private Map<String, Integer> libraryApis;
  private List<List<String>> items;

  public HashSolver() {
    libraryApis = new HashMap<>();
    items = new ArrayList<>();
  }

  public Map<String, Integer> getLibraryApis() {
    return libraryApis;
  }

  public void loadApis() {
    try {
      File apiFile = new File(Path.APIs);
      BufferedReader reader = new BufferedReader(new FileReader(apiFile));
      String line = null;
      int hashCode = 0;
      while ((line = reader.readLine()) != null) {
        libraryApis.put(line, hashCode);
        hashCode++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String locateApiFullName(List<String> libs, String shortName) {
    for (String lib: libs) {
      String candidate = lib + ".*." + shortName;
      String suffix = "." + shortName;
      for (String api: libraryApis.keySet()) {
        // System.out.println(candidate + " " + api);
        if (api.endsWith(suffix) && Pattern.matches(candidate, api)) return api;
      }
    }
    return "";
  }

  public List<List<String>> solve() {
//    for (SCSFile file: scsFiles) {
//      List<String> importedLibraries = file.getImportedLibraries();
//      for (SCSUnit unit: file.getUnits()) {
//        List<String> item = new ArrayList<>();
//        for (String shortName: unit.getInvocationShortNames()) {
//          String fullName = locateApiFullName(importedLibraries, shortName);
//          if (fullName.length() == 0) continue;
//          item.add(fullName);
//        }
//        items.add(item);
//      }
//    }
    return items;
  }

  public static void main(String[] args) {
    HashSolver solver = new HashSolver();
    solver.loadApis();
    File projectDir = new File("data/test");
    CallSeqExtractor.extract(projectDir);
    Map<String, Integer> map = solver.getLibraryApis();
    for (List<String> item: solver.solve()) {
      if (item.size() == 0) continue;
      System.out.println(item.toString());
      List<Integer> numbers = new ArrayList<>();
      for (String str: item) numbers.add(map.get(str));
      System.out.println(numbers.toString());
    }

  }

}
