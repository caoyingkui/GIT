package fileDiff.modify.type.concurrent;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

/**
 * Created by kvirus on 2019/5/26 17:06
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SYNCHRONIZED)
public class SynchronizedInsert extends Modify {
    public static int count = 0;
    public static SynchronizedInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        SynchronizedInsert result = new SynchronizedInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            //TODO
        }
        if (s) {
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
                "8892c0d9aff9e8b7a6722a50a18908ef575276da"
        };

        String[] fileNames = {
                "lucene/test-framework/src/java/org/apache/lucene/store/MockDirectoryWrapper.java"

        };

        String[] methodNames = {
                "createOutput"
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
                    SynchronizedInsert result = SynchronizedInsert.match(method);
                    int a = 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
