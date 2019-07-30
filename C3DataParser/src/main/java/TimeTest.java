import com.google.gson.Gson;
import fileDiff.Change;
import fileDiff.group.hash.StatementHash;
import fileDiff.method.MethodDiff;
import git.GitAnalyzer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * Created by kvirus on 2019/6/23 9:51
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class TimeTest {

    static List<Change<String>> getMethods(String path) {
        try {
            List<Change<String>> result = new ArrayList<>();
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createJsonParser(new File(path));

            JsonToken current = jp.nextToken(); // "{"
            current = jp.nextToken(); // "methods";
            current = jp.nextToken(); // "["
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = jp.readValueAsTree();
                String newContent = node.get("newContent").getTextValue();
                String oldContent = node.get("oldContent").getTextValue();
                result.add(new Change<String>(newContent, oldContent));
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void run(String path) {
        List<Change<String>> methods = getMethods(path);
        int len = methods.size();
        System.out.println("Total Method Amount: " + methods.size());


        List<List<StatementHash>> hashes  = getHashes(methods);
        long cur = System.currentTimeMillis();
        long totalHash = 0;
        long strlen = 0;
        double[] dis = new double[20];
        for (int i = 0; i < len; i++) {
            int hashesNum = hashes.get(i).size();
            dis[hashesNum > 19 ? 19 : hashesNum] ++;
            totalHash += hashesNum;
            for (StatementHash hash: hashes.get(i))
                strlen += hash.content.length();
            if (i % 1000 == 0)
                System.out.println(String.format("\tfinish " + (i / (len / 100))) + "%");
            //if (true) continue;
            for (int j = i + 1; j < len; j ++) {
                boolean r = MethodDiff.isSimilar(hashes.get(i), hashes.get(j)) > 0;
            }
        }
        for (int i = 0; i < 20; i ++)
            System.out.print(String.format("%.2f\t", dis[i] / len % 100));
        System.out.println();

        System.out.println("average hash: " + (totalHash / (double) len));
        System.out.println("average length: " + (strlen / (double) len));
        System.out.println("Total Time: " + (System.currentTimeMillis() - cur));
    }


    static List<List<StatementHash>> getHashes(List<Change<String>> methods) {
        List<List<StatementHash>> result = new ArrayList<>();

        for (Change<String> method: methods) {
            result.add(MethodDiff.getHashes(method.NEW, method.OLD));
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
        for (ProjectInfo.Project project: ProjectInfo.projects) {
            System.out.println("\n\nTime Test For \"" + project.name + "\"");
            if (!new File(project.storePath).exists())
                DataGenerator.generate(project.originalFile, new GitAnalyzer(project.gitPath), project.storePath);

            run(project.storePath);
        }
    }
}
