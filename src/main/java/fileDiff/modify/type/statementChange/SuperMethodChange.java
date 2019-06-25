package fileDiff.modify.type.statementChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.Diff;
import fileDiff.field.ChangedField;
import fileDiff.file.ChangedClass;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

/**
 * Created by kvirus on 2019/5/26 21:12
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.STATEMENT)
public class SuperMethodChange extends Modify {
    public static int count = 0;
    public String methodName = "";

    public static SuperMethodChange match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        SuperMethodChange result = new SuperMethodChange();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            if (!( change.getChangedEntity().getType() == JavaEntityType.CONSTRUCTOR_INVOCATION &&
                    change.getChangedEntity().getUniqueName().startsWith("super("))) return null;

            try {
                result.methodName = ((ChangedClass) method.file).parent.NEW;
            } catch (Exception e) {
                ;
            }

        }
        if (result.methodName.length() > 0) {
            count ++;
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
                "914e2641658b71638f9f4c940fe9f63b1a67ce7d"
        };

        String[] fileNames = {
                "lucene/core/src/java/org/apache/lucene/search/HitQueue.java"
        };

        String[] methodNames = {
                "HitQueue"
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
                    SuperMethodChange result = SuperMethodChange.match(method);
                    int a = 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
