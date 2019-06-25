package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
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
import git.GitAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/20 14:25
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ParameterModify extends Modify {
    public static int count = 0;

    List<Change<String>> parameters = new ArrayList<>();

    public static ParameterModify match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        ParameterModify result = new ParameterModify();
        ChangedMethod cMethod = (ChangedMethod) method;
        Set<String> oldTokens = new HashSet<>();
        Set<String> newTokens = new HashSet<>();

        Set<SourceCodeChange> toCheck = new HashSet<>();
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            ChangeType type = change.getChangeType();
            if (type == ChangeType.PARAMETER_INSERT) {
                Insert insert = (Insert) change;
                String newPara = insert.getChangedEntity().getUniqueName();
                newTokens.add(newPara);
                result.parameters.add(new Change<String>( newPara, "" ));
            } else if (type == ChangeType.PARAMETER_DELETE) {
                Delete delete = (Delete) change;
                String oldPara = delete.getChangedEntity().getUniqueName();
                oldTokens.add(oldPara);
                result.parameters.add(new Change<String> ("", oldPara));
            } else if (type == ChangeType.PARAMETER_RENAMING) {
                Update update = (Update) change;
                String newToken = update.getNewEntity().getUniqueName();
                String oldToken = update.getChangedEntity().getUniqueName();

                oldTokens.add(oldToken);
                newTokens.add(newToken);
                result.parameters.add(new Change<String>(newToken, oldToken));
            } else {
                toCheck.add(change);
            }
        }

        for (SourceCodeChange change: toCheck) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            String newStr = "", oldStr = "";
            if (change instanceof Insert) {
                Insert insert = (Insert) change;
                newStr = insert.getChangedEntity().getUniqueName();
            } else if (change instanceof Update) {
                Update update = (Update) change;
                newStr = update.getNewEntity().getUniqueName();
                oldStr = update.getChangedEntity().getUniqueName();
            } else if (change instanceof Delete) {
                Delete delete = (Delete) change;
                oldStr = delete.getChangedEntity().getUniqueName();
            }

            boolean newSig = true, oldSig = true;
            if (newStr.length() > 0) {
                newSig = false;
                for (String token: newTokens) {
                    if (newStr.contains(token)) {
                        newSig = true;
                        break;
                    }
                }
            }

            if (oldStr.length() > 0) {
                oldSig = false;
                for (String token: oldTokens) {
                    if (oldStr.contains(token)) {
                        oldSig = true;
                        break;
                    }
                }
            }

            if (!( newSig || oldSig )) return null;

        }

        if (result.parameters.size() > 0) {
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
                "7d8fc543f01a31c8439c369b26c3c8c5924ba4ea",
                "1118299c338253cea09640acdc48dc930dc27fda",
        };

        String[] fileNames = {
                "lucene/core/src/java/org/apache/lucene/search/TopFieldCollector.java",
                "lucene/test-framework/src/java/org/apache/lucene/index/AssertingLeafReader.java"
        };

        String[] methodNames = {
                "populateResults",
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
                    ParameterModify result = ParameterModify.match(method);
                    int a = 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
