package fileDiff.modify.type.signatureChange;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/20 14:05
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ParameterRename extends Modify {
    public static int count = 0;
    public List<Change> parameters = new ArrayList<>();

    public static ParameterRename match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ParameterRename result = new ParameterRename();
        ChangedMethod cMethod = (ChangedMethod) method;
        List<Change<String>> paras = new ArrayList<>();
        for (SourceCodeChange change: cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.PARAMETER_RENAMING) {
                Update update = (Update)change;
                paras.add(new Change<String> (
                        update.getNewEntity().getUniqueName(),
                        update.getChangedEntity().getUniqueName())
                );
            }
        }

        if (paras.size() > 0) s = true;

        for (SourceCodeChange change: cMethod.getSourceCodeChanges()) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            if (!( change instanceof Update )) return null;

            Update update = (Update) change;
            String oldStr = update.getChangedEntity().getUniqueName();
            String newStr = update.getNewEntity().getUniqueName();
            boolean sig = false;
            for (Change<String> para: paras) {
                if (oldStr.contains(para.OLD) && newStr.contains(para.NEW)) {
                    sig = true;
                    result.parameters.add(para);
                }
            }
            if (sig == false) return null;
        }

        if (s) {
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
                    ParameterRename result = ParameterRename.match(method);
                    int a = 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
