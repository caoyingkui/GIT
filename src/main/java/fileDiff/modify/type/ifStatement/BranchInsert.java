package fileDiff.modify.type.ifStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
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
 * Created by kvirus on 2019/5/20 23:15
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.IF)
public class BranchInsert extends Modify {
    public static int count = 0;
    public static BranchInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        BranchInsert result = new BranchInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        List<SourceCodeChange> toCheck = new ArrayList<>();
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change instanceof Insert &&
                    change.getChangedEntity().getType() == JavaEntityType.ELSE_STATEMENT) {
                int start = change.getChangedEntity().getStartPosition();
                int end = change.getChangedEntity().getEndPosition();
                ranges.add(new Pair<Integer, Integer>(start, end));
            } else {
                toCheck.add(change);
            }
        }

        if (ranges.size() == 0) return null;

        toCheck.removeIf(change -> {
           if (change instanceof Insert &&
                change.getChangedEntity().getType() == JavaEntityType.IF_STATEMENT) {
               int start = change.getChangedEntity().getStartPosition();
               int end = change.getChangedEntity().getEndPosition();
               boolean sig = false;
               for (Pair<Integer, Integer> p: ranges) {
                   if ( start == p.getKey() && end == p.getValue()) {/*插入的else语句*/
                       sig = true;
                   } else if (p.getKey() >= start && p.getValue() <= end) { // 插入头一个if
                       sig = true;
                       ranges.add(new Pair<Integer, Integer>(start, end));
                   }
                   if (sig) break;
               }
               return sig;
           } else {
               return false;
           }
        });

        for (SourceCodeChange change: toCheck) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            int start = -1, end = -1;
            if (change instanceof Insert) {
                start = change.getChangedEntity().getStartPosition();
                end = change.getChangedEntity().getEndPosition();
            } else if (change instanceof Move && change.getChangedEntity().getType() == JavaEntityType.IF_STATEMENT){
                start = change.getChangedEntity().getStartPosition();
                end = change.getChangedEntity().getEndPosition();
            } else {
                return null;
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

        count ++;
        return result;
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
                "8892c0d9aff9e8b7a6722a50a18908ef575276da",
                "5ef67e9f17e11d77c1439f5320af47962a4451e6",
                "a0bb5017722ce698fc390f3990243697341d2b8d"
        };

        String[] fileNames = {
                "lucene/core/src/java/org/apache/lucene/store/Directory.java",
                "lucene/core/src/java/org/apache/lucene/index/FrozenBufferedUpdates.java",
                "solr/solrj/src/java/org/apache/solr/client/solrj/io/stream/LetStream.java"
        };

        String[] methodNames = {
                "createOutput",
                "applyQueryDeletes",
                "LetStream"
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
                    BranchInsert result = BranchInsert.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
