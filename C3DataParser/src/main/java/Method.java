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
    public String fileName = "";
    public String newName = "";
    public String oldName = "";

    public String newContent = "";
    public String oldContent = "";

    public String toJson() {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("newContent", newContent);
        o.put("oldContent", oldContent);
        return o.toString();
    }
}
