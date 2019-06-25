package util;

import javafx.util.Pair;

import java.util.List;

/**
 * Created by kvirus on 2019/5/24 21:56
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class RangeTool {
    public static boolean contains(List<Pair<Integer, Integer>> ranges, int start, int end) {
        for (Pair<Integer, Integer> p: ranges) {
            if (p.getKey() <= start && p.getValue() >= end)
                return true;
        }
        return false;
    }
}
