package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

/**
 * Created by kvirus on 2019/5/18 20:31
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class Rename extends Modify {
    public static int count = 0;

    public static Rename match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        Rename result = new Rename();
        ChangedMethod cMethod = (ChangedMethod)method;
        for (SourceCodeChange change: cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.METHOD_RENAMING) {
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

    public static void main(String[] args) {
            GitAnalyzer git = new GitAnalyzer();
            Diff d = new Diff(git, git.getId("62130ae70ceccfb395052446cdb32a44c7fc23ac"));
            FileDiff c = null;
            String targetName = "lucene/core/src/java/org/apache/lucene/index/DocumentsWriterPerThread.java";
            targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
            //targetName = "MultiComparatorLeafCollector";
            String methodName = "reserveOneDoc";
            for (FileDiff file: d.getClasses()) {
                if (file.getName().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    Rename result = Rename.match(method);
                }
            }
        }
}
