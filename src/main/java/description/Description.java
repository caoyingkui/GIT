package description;

import analyzer.histories.Issue;
import javafx.util.Pair;
import util.SetTool;
import util.StemTool;

import java.io.*;
import java.util.*;

/**
 * Created by kvirus on 2019/5/2 13:54
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Description {
    public String description = "";
    public Set<String> tokens;
    public Set<String> stemmedTokens = new HashSet<>();
    public Set<String> APIs = new HashSet<>();
    public static Map<String, Double> IDF = new HashMap<>();
    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("issueCrawler/idf.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] idf = line.split(" ");
                IDF.put(idf[0], Double.parseDouble(idf[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Description(String des) {
        this.description = des;
        tokens = SetTool.toSet(des.split("[^a-zA-Z0-9_]"));
        tokens.removeIf(token -> StemTool.isStopWord(token));
        stemmedTokens = new HashSet<>();
        stemmedTokens.addAll(StemTool.stem(StemTool.tokenize(des)));
    }

    public void extractAPIs(Set<String> apis) {
        APIs = new HashSet<>();
        for (String token: tokens) {
            if (apis.contains(token))
                APIs.add(token);
        }
    }

    public static void getIDF() {
        Map<String, Double> count = new HashMap<>();
        File dir = new File("issueCrawler/issue");
        File[] files = dir.listFiles();
        int issueCount = 0;
        for (File file: files) {
            String fileName = file.getName();
            if (fileName.endsWith(".json")) {
                issueCount ++;
                String issueId = fileName.replace(".json", "");
                Issue issue = new Issue(issueId);
                String content = issue.description;
                List<String> tokens = StemTool.stem(content);
                for (String token : new HashSet<String>(tokens)) {
                    if (token.equals("version")) {
                        int a = 2;
                    }
                    count.put(token, count.getOrDefault(token, 0.0) + 1);
                }
            }
        }

        List<Pair<String, Double>> list = new ArrayList<>();
        for (String token: count.keySet()) {
            double idf = Math.log(issueCount / (count.get(token) + 1));
            list.add(new Pair<String, Double>(token ,idf));
        }
        list.add(new Pair<String, Double>("NULL", Math.log(issueCount)));
        Collections.sort(list, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o1.getValue() > o2.getValue() ? 1 : (o1.getValue() == o2.getValue() ? 0 : -1);
            }
        });

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("issueCrawler/idf.txt")));
            for (Pair<String, Double> p : list) {
                String line = p.getKey() + " " + p.getValue() + "\n";
                writer.write(line);
            }
            writer.close();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Description.getIDF();
    }
}
