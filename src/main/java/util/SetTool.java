package util;

import fileDiff.Change;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/4/19 11:40
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class SetTool{

    public static Set<String> insert(Set<String> set1, Set<String> set2) {
        HashSet<String> result = new HashSet<>();
        for (String str1: set1) {
            if (set2.contains(str1))
                result.add(str1);
        }
        return result;
    }

    public static HashSet<String> difference(Set<String> set1, Set<String> set2) {
        HashSet<String> result = new HashSet<>();
        for (String str1: set1) {
            if (!set2.contains(str1))
                result.add(str1);
        }
        return result;
    }

    public static HashSet<String> union(Set<String> set1, Set<String> set2, Set<String> ... sets) {
        HashSet<String> result = new HashSet<>();
        result.addAll(set1);
        result.addAll(set2);
        for (Set<String> s: sets)
            result.addAll(s);
        return result;
    }

    public static HashSet<String> toSet(List<String> list) {
        HashSet<String> result = new HashSet<>();
        for (String item: list) {
            result.add(item);
        }
        return result;
    }

    public static HashSet<String> toSet(String[] arr) {
        HashSet<String> result = new HashSet<>();
        for (String s: arr)
            result.add(s);
        return result;
    }

    public static HashSet<String> toSet(String code) {
        return toSet(code.split("[^a-zA-Z0-9_]"));
    }
}
