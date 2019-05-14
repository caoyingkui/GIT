package fileDiff.diff;

import analyzer.histories.variation.Mutant;
import analyzer.histories.variation.MutantType;
import fileDiff.Change;
import gumtree.GumTree;
import util.ReaderTool;

import java.util.*;

/**
 * Created by kvirus on 2019/4/18 16:11
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Util {
    public static List<Change<String>> getUpdateTokens(String newContent, String oldContent) {
        List<Change<String>> result = new ArrayList<>();
        List<Mutant> mutants = GumTree.getDifference(newContent, oldContent);
        mutants.stream().filter(m -> m.type == MutantType.UPDATE).forEach(m -> {
            result.add(new Change<String>(m.after, m.before));
        });

        return result;
    }

    public static void main(String[] args) {
        String s1 = ReaderTool.read("file1.java");

        String s2 = ReaderTool.read("file2.java");

        for (Change<String> s: getUpdateTokens(s1, s2)) {
            System.out.println(s.OLD + " " + s.NEW);
        }
    }
}
