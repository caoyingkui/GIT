import fileDiff.Change;
import fileDiff.method.MethodDiff;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by kvirus on 2019/6/28 10:30
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ClusterTest implements Runnable{
    public String path;

    public ClusterTest(String path) {
        this.path = path;
    }

    private List<Method> getMethods() {
        List<Method> result = new ArrayList<>();
        try {
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createJsonParser(new File(path));
            JsonToken current = jp.nextToken();
            current = jp.nextToken();
            if (current != JsonToken.FIELD_NAME && !jp.getCurrentName().equals("methods")) return null;

            current = jp.nextToken();
            if (current != JsonToken.START_ARRAY) return null;
            while ((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                JsonNode node = jp.readValueAsTree();
                result.add(new Method(node));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Cluster> getCluster() {
        List<Method> methods = getMethods();
        Map<String, List<Method>> c = methods.stream().
                collect(Collectors.groupingBy(method -> method.clusterId));
        List<Cluster> clusters = new ArrayList<>();
        for (String id: c.keySet()) {
            Cluster cluster = new Cluster();
            cluster.methods.addAll(c.get(id));
            clusters.add(cluster);
        }
        return clusters;
    }


    @Override
    public void run() {
        List<Method> methods = getMethods();
        int len = methods.size();
        int total = 0, cor = 0, wor = 0;
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j ++) {
                total ++;
                Method m1 = methods.get(i), m2 = methods.get(j);
                boolean s = m1.clusterId.equals(m2.clusterId);
                if (s && MethodDiff.isSimilar(new Change<String>(m1.newContent, m1.oldContent),
                        new Change<String>(m2.newContent, m2.oldContent)) > 0) cor ++;
                else wor ++;
            }
        }
        System.out.println(total + " " + cor + " " + wor);
    }

    public static void main(String[] args) {
        String path = "C:\\Users\\oliver\\Desktop\\json1.json";
        ClusterTest test = new ClusterTest(path);
        test.run();
    }
}
