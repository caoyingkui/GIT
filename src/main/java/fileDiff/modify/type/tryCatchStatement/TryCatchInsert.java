package fileDiff.modify.type.tryCatchStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
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
 * Created by kvirus on 2019/5/24 13:40
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.TRY)
public class TryCatchInsert extends Modify {
    public static int count = 0;

    public static TryCatchInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        TryCatchInsert result = new TryCatchInsert();
        ChangedMethod cMethod = (ChangedMethod) method;

        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (!( change instanceof Insert )) continue;
            if (change.getChangedEntity().getType() == JavaEntityType.TRY_STATEMENT) {
                s = true;
                break;
            }
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

        };

        String[] fileNames = {

        };

        String[] methodNames = {

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
                    TryCatchInsert result = TryCatchInsert.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
