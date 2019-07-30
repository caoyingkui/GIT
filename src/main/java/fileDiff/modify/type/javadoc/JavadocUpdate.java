package fileDiff.modify.type.javadoc;

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

/**
 * Created by kvirus on 2019/5/20 9:07
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.JAVADOC)
public class JavadocUpdate extends Modify {
    public static int count = 0;
    public Change<String> javadoc;
    public static JavadocUpdate match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        JavadocUpdate result = new JavadocUpdate();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.DOC_UPDATE) {
                Update update = (Update) change;
                String newDoc = update.getNewEntity().getUniqueName();
                String oldDoc = update.getChangedEntity().getUniqueName();
                result.javadoc = new Change<String>(newDoc, oldDoc);
                s = true;
                break;
            }
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

    public static void test() {
        String[] ids = {
                "77a4bfaa90637cd3d9a8a2ef4889e163dab143aa"
        };

        String[] fileNames = {
                "solr/core/src/java/org/apache/solr/schema/FieldType.java"
        };

        String[] methodNames = {
                "getUninversionType"
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
                    JavadocUpdate result = JavadocUpdate.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
