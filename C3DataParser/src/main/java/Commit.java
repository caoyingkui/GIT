import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class Commit {
    public String id;
    public Set<String> methodIds = new HashSet<String>();
    public List<Method> methods = new ArrayList<Method>();

    public void addMethod(Method m) {
        if (methodIds.contains(m.id)) return;
        methodIds.add(m.id);
        methods.add(m);
    }
}
