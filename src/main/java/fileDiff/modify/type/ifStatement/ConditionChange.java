package fileDiff.modify.type.ifStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/5/20 9:33
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.IF)
public class ConditionChange extends Modify {
    public static int count = 0;
    public List<Change<String>> conditions = new ArrayList<>();

    public static ConditionChange match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        ConditionChange result = new ConditionChange();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            if (change.getChangeType() == ChangeType.CONDITION_EXPRESSION_CHANGE) {
                Update update = (Update) change;
                String newCondition = update.getNewEntity().getUniqueName();
                String oldCondition = update.getChangedEntity().getUniqueName();
                result.conditions.add(new Change<String>(newCondition, oldCondition));
            } else {
                return null;
            }
        }
        if (result.conditions.size() > 0) {
            count++;
            return result;
        } else {
            return null;
        }
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
                "1118299c338253cea09640acdc48dc930dc27fda",
                "77a4bfaa90637cd3d9a8a2ef4889e163dab143aa"
        };

        String[] fileNames = {
                "lucene/test-framework/src/java/org/apache/lucene/codecs/asserting/AssertingPointsFormat.java",
                "solr/core/src/java/org/apache/solr/search/facet/FacetField.java"
        };

        String[] methodNames = {
                "writeField",
                "createFacetProcessor"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < 1; i++) {
            Diff d = new Diff(git, git.getId(ids[i]));
            FileDiff c = null;
            String targetName = fileNames[i];
            String methodName = methodNames[i];
            for (FileDiff file: d.getClasses()) {
                if (file.getPath().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    ConditionChange result = ConditionChange.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
