package fileDiff.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/27 18:08
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class GroupHash {
    public static int calculateHash(Set<String> tokens) {
        List<String> tokenList = new ArrayList<>(tokens);
        tokenList.sort(Comparator.naturalOrder());
        StringBuilder appendString = new StringBuilder();
        for (String s: tokens) {
            appendString.append(s);
        }
        return appendString.toString().hashCode();
    }
}
