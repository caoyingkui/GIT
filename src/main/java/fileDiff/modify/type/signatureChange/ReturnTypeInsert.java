package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import fileDiff.Diff;
import fileDiff.file.ChangedClass;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

/**
 * Created by kvirus on 2019/5/19 20:09
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ReturnTypeInsert extends Modify {
    public static int count = 0;
    public String returnType = "";

    public static ReturnTypeInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ReturnTypeInsert result = new ReturnTypeInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() ==  ChangeType.RETURN_TYPE_INSERT) {
                SourceCodeEntity entity = change.getChangedEntity();
                int start = entity.getStartPosition();
                int end = entity.getEndPosition() + 1;
                result.returnType = ((ChangedClass)cMethod.file).content.NEW.substring(start, end);
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
            String targetName = "lucene/core/src/java/org/apache/lucene/index/DocumentsWriter.java";
            targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
            //targetName = "MultiComparatorLeafCollector";
            String methodName = "abortThreadState";
            for (FileDiff file: d.getClasses()) {
                if (file.getName().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    ReturnTypeInsert result = ReturnTypeInsert.match(method);
                }
            }
        }
}
