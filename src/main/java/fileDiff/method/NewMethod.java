package fileDiff.method;

import analyzer.histories.Comment;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.type.DiffType;
import git.Method;

import java.util.HashSet;

/**
 * Created by kvirus on 2019/4/20 16:48
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class NewMethod extends MethodDiff implements MethodUpdate, FieldUpdate{
    public DiffType type;

    public String name;

    public String fullName;

    public String signature;

    public String content;

    public String comment;

    public HashSet<String> tokens;

    public NewMethod(Method method, FileDiff file, String commitId) {
        this.commitId = commitId;

        name = method.name;
        fullName = method.fullName;
        signature = getSignature(method.fullName);
        content = method.methodContent;
        comment = method.comment;
        this.file = file;

        extractChangedTokens();
        getType();
    }

    @Override
    public int commentWords(Comment comment) {
        int count = 0;
        String content = comment.content;
        for (String token: tokens)
            if (content.contains(token))
                count ++;
        return count;
    }

    @Override
    public int commonKeyWords(Comment comment) {
        int count = 0;
        String content = comment.content;
        for (String token: addWords ) {
            if (content.contains(token)) count ++;
        }

        for (String token: changedWords) {
            if (content.contains(token)) count ++;
        }

        if (comment.content.contains(name))
            count += 5;

        return count;
    }

    @Override
    public void extractChangedTokens() {
        tokens = MethodDiff.extractTokens(content);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void fieldUpdate(Diff diff) {
        for (String token: diff.newFields.keySet())
            if (tokens.contains(token))
                addWords.add(token);

        for (String token: diff.newMethods.keySet())
            if (tokens.contains(token))
                addWords.add(token);

        for (String field: diff.changedMethods.keySet())
            if (tokens.contains(field))
                addWords.add(field);
    }

    @Override
    public String highlighter(String content) {
        return super.highlighter(content);
    }

    @Override
    public void parseInterfaceRelation(MethodDiff method) {
        if (method instanceof NewMethod) {
            ((NewMethod) method).type = DiffType.Method_Add_By_Interface;
        }
    }

    @Override
    public void parseRefactorRelation(MethodDiff method) {
        if (method instanceof NewMethod) {
            ((NewMethod) method).type = DiffType.Method_Add_By_Refactor;
        }
    }

    @Override
    public void update(Diff diff) {
        fieldUpdate(diff);
    }

    @Override
    public String toString() {
        return name;
    }
}
