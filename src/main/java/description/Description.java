package description;

import analyzer.histories.Issue;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.MethodDiff;
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
    public int id = 0;

    public String description = "";
    public Set<String> tokens;
    public Set<String> stemmedTokens = new HashSet<>();
    public static Map<String, Double> IDF = new HashMap<>();

    //classes 记录了description中出现的类的名字
    public Set<String> classes = new HashSet<>();

    //methods 记录了description中出现的方法的名字
    public Set<String> methods = new HashSet<>();

    //fields 记录了description中出现的域的名字
    public Set<String> fields = new HashSet<>();

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

    public void recognize(Diff diff) {
        for (FileDiff file: diff.getClasses()) {
            String name = file.getName();
            if (tokens.contains(name)) {
                classes.add(name);
                recognizeMethod(file);
            } else {
                recognizeMethod(diff.getClasses());
            }
        }
    }

    private void recognizeMethod(List<FileDiff> files) {
        for (FileDiff file: files)
            recognizeMethod(file);
    }

    private void recognizeMethod(FileDiff file) {
        boolean s = false;
        //判断文本中是否有完整的方法名
        for (MethodDiff m: file.getMethods()) {
            String name = m.getName();
            if (tokens.contains(name)) {
                s = true;
                methods.add(name);
            }
        }

        //如果没有完整的方法名
        //需要一些启发式的规则去判断
        /*if (!s) {
            for (MethodDiff m: file.getMethods()) {
                String name = m.getName();
                List<String> tokenInName = StemTool.stem(StemTool.camelCase(name));
                for (String t: tokenInName) {
                    if (stemmedTokens.contains(t)) {
                        methods.add(name);
                    }
                }
            }
        }*/
    }

    public void recognize(Set<String> classes, Set<String> methods, Set<String> fields) {
        for (String clazz: classes) {
            if (tokens.contains(clazz))
                this.classes.add(clazz);
        }

        for (String method: methods) {
            if (tokens.contains(method))
                this.methods.add(method);
        }

        for (String field: fields) {
            if (tokens.contains(field))
                this.fields.add(field);
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

    @Override
    public String toString() {
        return description;
    }


    public static void main(String[] args) {
        Description.getIDF();
    }
}
