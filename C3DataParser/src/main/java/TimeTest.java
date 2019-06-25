import com.google.gson.Gson;
import fileDiff.Change;
import fileDiff.group.hash.StatementHash;
import fileDiff.method.MethodDiff;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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

    static List<List<StatementHash>> getHashes(List<Change<String>> methods) {
        List<List<StatementHash>> result = new ArrayList<>();

        for (Change<String> method: methods) {
            result.add(MethodDiff.getHashes(MethodDiff.getChanges(method.NEW, method.OLD)));
        }
        return result;
    }

    public static void main(String[] args) {

        char[][] result = new char[4000][4000];

        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\json1.json");
        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_junit_results_combined1.json");

        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_eclipseswt_results_combined1.json");


        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_fitlibrary_results_combined1.json");
        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_checkstyle_results_combined1.json");

        //List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_checkstyle_results_combined1.json");
        List<Change<String>>        methods = getMethods("C:\\Users\\oliver\\Desktop\\ares_eclipsejdt_results_combined1.json");
        List<List<StatementHash>>   hashes  = getHashes(methods);
        long cur = System.currentTimeMillis();
        int len = hashes.size();
        for (int i = 0; i < len; i++) {
            if (i % 1000 == 0)
                System.out.println(i);
            for (int j = i + 1; j < len; j ++) {
                boolean r = MethodDiff.isSimilar(hashes.get(i), hashes.get(j));
            }
        }
        System.out.println(len);
        System.out.println(System.currentTimeMillis() - cur);
    }
}
