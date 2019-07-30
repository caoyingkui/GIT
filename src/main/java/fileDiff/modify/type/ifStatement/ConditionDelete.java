package fileDiff.modify.type.ifStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/5/20 19:17
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.IF)
public class ConditionDelete extends Modify {
    public static int count = 0;
    public List<String> conditions = new ArrayList<String>();

    public static ConditionDelete match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ConditionDelete result = new ConditionDelete();
        ChangedMethod cMethod = (ChangedMethod) method;

        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        cMethod.getSourceCodeChanges().stream().filter(change -> {
            return change instanceof Delete && change.getChangedEntity().getType() == JavaEntityType.IF_STATEMENT;
        }).forEach(change -> {
            int start = change.getChangedEntity().getStartPosition();
            int end = change.getChangedEntity().getEndPosition();
            ranges.add(new Pair<Integer, Integer>(start, end));
            result.conditions.add(change.getChangedEntity().getUniqueName());
        });

        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            int start = -1, end = -1;
            if (change instanceof Insert) {
                return null;
            } else if (change instanceof Move) {
                Move move = (Move) change;
                start = move.getChangedEntity().getStartPosition();
                end = move.getChangedEntity().getEndPosition();
            }  else if (change instanceof Delete) {
                start = change.getChangedEntity().getStartPosition();
                end = change.getChangedEntity().getEndPosition();
            } else if (change instanceof Update) {
                continue;
            }

            boolean sig = false;
            for (Pair<Integer, Integer> p: ranges) {
                if (p.getKey() <= start && p.getValue() >= end) {
                    sig = true;
                    break;
                }
            }
            if (!sig) return null;
        }
        return ranges.size() > 0 && (++count) > 0 ? result : null;
    }

    @Override
    protected void build() {

    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void extend(String str) {
        super.extend(str);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    public static void test() {
        String[] ids = {
                "d78feb22361cef5323793fdff33de621320d7b4b",
                "5a4fd86cce57586dd14fb8f11f9b170b121eebc4"
        };

        String[] fileNames = {
                "lucene/core/src/java/org/apache/lucene/util/automaton/DaciukMihovAutomatonBuilder.java",
                "lucene/highlighter/src/java/org/apache/lucene/search/highlight/WeightedSpanTermExtractor.java"
        };

        String[] methodNames = {
                "add",
                "WeightedSpanTermExtractor"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < ids.length; i++) {
            Diff d = new Diff(git, git.getId(ids[i]));
            FileDiff c = null;
            String targetName = fileNames[i];
            targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
            String methodName = methodNames[i];
            for (FileDiff file: d.getClasses()) {
                if (file.getName().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    ConditionDelete result = ConditionDelete.match(method);

                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
