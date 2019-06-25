package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import fileDiff.modify.type.statementChange.FieldChange;
import git.GitAnalyzer;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/5/18 8:32
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ParameterInsert extends Modify {
    public static int count = 0;
    public List<String> addedFields = new ArrayList<>();

    public static ParameterInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ParameterInsert result = new ParameterInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        cMethod.getSourceCodeChanges().stream().filter(change -> change.getChangeType() == ChangeType.PARAMETER_INSERT)
                .forEach(change -> {
                    result.addedFields.add(change.getChangedEntity().getUniqueName());
                });

        for (SourceCodeChange change: cMethod.getSourceCodeChanges()) {
            String str = "";
            if (change instanceof Update) {
                str = ((Update) change).getNewEntity().getUniqueName();
            } else if (change instanceof Insert) {
                str = ((Insert) change).getChangedEntity().getUniqueName();
            }
            boolean sig = false;
            for (String token: result.addedFields) {
                if (str.contains(token)) {
                    sig = true;
                    break;
                }
            }
            if (!sig) return null;
        }

        return s && (++count) > 0 ? result : null;
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

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        String[] ids = {
                "1118299c338253cea09640acdc48dc930dc27fda"
        };

        String[] fileNames = {
                "lucene/test-framework/src/java/org/apache/lucene/index/AssertingLeafReader.java"
        };

        String[] methodNames = {
                "AssertingIntersectVisitor"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < 1; i++) {
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
                    ParameterInsert result = ParameterInsert.match(method);
                    int a = 2;
                }
            }
        }
    }
}
