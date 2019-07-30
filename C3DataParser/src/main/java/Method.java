import org.codehaus.jackson.JsonNode;
import org.json.JSONObject;

/**
 * Created by kvirus on 2019/6/22 23:09
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Method {
    public String id;
    public String clusterId;
    public String fileName = "";
    public String newName = "";
    public String oldName = "";

    public String newContent = "";
    public String oldContent = "";

    public Method() {
        ;
    }

    public Method(JsonNode node) {
        id          = node.get("id").getTextValue();
        clusterId   = node.get("clusterId").getTextValue();
        newContent  = node.get("newContent").getTextValue();
        oldContent  = node.get("oldContent").getTextValue();
    }

    public String toJson() {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("clusterId", clusterId);
        o.put("newContent", newContent);
        o.put("oldContent", oldContent);
        return o.toString();
    }
}
