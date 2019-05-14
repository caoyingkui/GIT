package fileDiff.method;

import analyzer.histories.Comment;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import description.Description;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.diff.Util;
import fileDiff.field.ChangedField;
import fileDiff.field.FieldDiff;
import fileDiff.file.ChangedClass;
import fileDiff.file.DelClass;
import fileDiff.file.FileDiff;
import fileDiff.modify.type.FieldReplace;
import fileDiff.modify.type.MethodReplace;
import fileDiff.modify.type.Modify;
import fileDiff.type.DiffType;
import git.GitAnalyzer;
import git.Method;
import javafx.util.Pair;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import util.SetTool;
import util.StemTool;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by kvirus on 2019/4/20 17:16
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ChangedMethod extends MethodDiff implements FieldUpdate{
    public DiffType type;
    public Change<String> name;
    public Change<String> signature;
    public Change<String> fullName;
    public Change<String> content;
    public Change<String> comment;
    public Change<MethodDeclaration> node;

    //tokens记录的是一个方法前后删除、增加的token
    public Change<HashSet<String>> tokens;

    public List<Change<String>> changedFields = null;

    public List<Change<String>> changedSection = new ArrayList<>();

    @NotNull
    private List<SourceCodeChange> sourceCodeChanges = new ArrayList<>();

    public ChangedMethod(Method newMethod, Method oldMethod, FileDiff file, String commitId) {
        this.commitId = commitId;
        name = new Change<>(newMethod.name, oldMethod.name);
        fullName = new Change<>(newMethod.fullName, oldMethod.fullName);
        signature = new Change<>(getSignature(newMethod.fullName), getSignature(oldMethod.fullName));
        content = new Change<>(newMethod.methodContent, oldMethod.methodContent);
        comment = new Change<>(newMethod.comment, oldMethod.comment);
        node = new Change<>(newMethod.node, oldMethod.node);
        this.file = file;

        getType();
    }

    public List<Change<String>> getChangedFiled() {
        if (changedFields != null) return changedFields;
        changedFields = Util.getUpdateTokens(content.NEW, content.OLD);
        return changedFields;
    }

    public void setSourceCodeChanges(List<SourceCodeChange> changes) {
        this.sourceCodeChanges = changes;
        extractChangedSection();
        extractChangedTokens();
    }

    public List<SourceCodeChange> getSourceCodeChanges() {
        return sourceCodeChanges;
    }

    public List<Modify> modifies = new ArrayList<>();

    @Override
    public int commentWords(Comment comment) {
        int count = 0;
        String content = comment.content;
        for (String token: tokens.NEW)
            if (content.contains(token)) count ++;
        for (String token: tokens.OLD)
            if (content.contains(token)) count ++;
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

        for (String token: delWords) {
            if (content.contains(token)) count ++;
        }

        if (comment.content.contains(name.NEW))
            count += 5;

        return count;
    }

    /**
     * 提取两段代码中差异的section
     */
    private void extractChangedSection() {
        changedSection = new ArrayList<>();

        if (!(file instanceof ChangedClass)) return;

        ChangedClass clazz = (ChangedClass)file;
        class T{
            int chStart;    //chStart修改前区域的起始位置
            int chEnd;      //chEnd修改前区域的终止位置，
            int neStart;    //neStart修改后区域的起始位置
            int neEnd;      //neEnd修改后区域的终止位置。

            public boolean merge(T t) {
                boolean res = false;
                if (chStart >= 0 && t.chStart >= 0) {
                    //相交
                    int min = Math.min(chStart, t.chStart), max = Math.max(chEnd, t.chEnd);

                    if(max - min <= chEnd - chStart + t.chEnd - t.chStart) {
                        chStart = min;
                        chEnd = max;
                        res = true;
                    }
                }

                if (neStart >= 0 && t.neStart >= 0) {
                    int min = Math.min(neStart, t.neStart), max = Math.max(neEnd, t.neEnd);

                    if(max - min <= neEnd - neStart + t.neEnd - t.neStart) {
                        neStart = min;
                        neEnd = max;
                        res = true;
                    }
                }
                return res;
            }
        };

        List<T> list = new ArrayList<>();
        for (SourceCodeChange change: sourceCodeChanges) {

            T t = new T();
            if (change instanceof Insert) {
                t.neStart = change.getChangedEntity().getStartPosition();
                t.neEnd = change.getChangedEntity().getEndPosition();
                t.chStart = t.chEnd = -1;
            } else if (change instanceof Move) {
                Move m = (Move) change;
                t.neStart = m.getNewEntity().getStartPosition();
                t.neEnd = m.getNewEntity().getEndPosition() + 1;
                t.chStart = m.getChangedEntity().getStartPosition();
                t.chEnd = m.getChangedEntity().getEndPosition() + 1;
            } else if (change instanceof Update) {
                Update u = (Update) change;
                t.neStart = u.getNewEntity().getStartPosition();
                t.neEnd = u.getNewEntity().getEndPosition() + 1;
                t.chStart = u.getChangedEntity().getStartPosition();
                t.chEnd = u.getChangedEntity().getEndPosition() + 1;
            } else if (change instanceof Delete) {
                Delete d = (Delete) change;
                t.neStart = t.neEnd = -1;
                t.chStart = d.getChangedEntity().getStartPosition();
                t.chEnd = d.getChangedEntity().getEndPosition() + 1;
            }

//            //因为在用changeDistiller时，只能解析类，不能直接解析函数
//            //所以为了能解析函数，我在函数的外层构造了"public class temp {METHOD}"，
//            //为了得到正确的内容，所以要对位置进行修正。
//            int correction = "public class temp {".length();
//            t.chStart -= correction;
//            t.chEnd -= correction;
//            t.neStart -= correction;
//            t.neEnd -= correction;

            boolean merge = false;
            for (T temp: list) {
                if (temp.merge(t)) {
                    merge = true;
                    break;
                }
            }
            if (!merge) {
                list.add(t);
            }
        }

        for(T temp: list) {
            //这说明该函数的函数前面发生了修改，暂时不处理
            if(temp.neStart == 0 || temp.chStart == 0)
                continue;
            try {
                String neSection = temp.neStart < 0 ? "" : clazz.content.NEW.substring(temp.neStart, temp.neEnd );
                String chSection = temp.chStart < 0 ? "" : clazz.content.OLD.substring(temp.chStart, temp.chEnd);
                if (neSection.equals(chSection)) continue;
                Change<String> section = new Change<>(neSection, chSection);
                changedSection.add(section);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        /*changedSection = new ArrayList<>();
        EditList list = GitAnalyzer.getEditList(content.OLD, content.NEW);

        if (list.size() == 0) return;
        Change<HashSet<Integer>> lines = new Change<HashSet<Integer>>(new HashSet<>(), new HashSet<>());

        String[] newLines = content.NEW.split("\\n");
        String[] oldLines = content.OLD.split("\\n");
        for (Edit edit: list) {
            StringBuilder newSection = new StringBuilder(), oldSection = new StringBuilder();
            for (int i = edit.getBeginA(); i < edit.getEndA(); i++) {
                oldSection.append(oldLines[i]).append("\n");
                lines.OLD.add(i);
            }
            for (int i = edit.getBeginB(); i < edit.getEndB(); i++) {
                newSection.append(newLines[i]).append("\n");
                lines.NEW.add(i);
            }

            //存储修改区域
            Change<String> section = new Change<String>(newSection.toString().trim(), oldSection.toString().trim());;
            if (section.NEW.length() > 0 || section.OLD.length() > 0) //有一些修改空行的情况
                changedSection.add(section);
        }*/
    }

    /**
     * 提取两段代码中差异的token
     */
    protected void extractChangedTokens() {
        Set<String> newTokens = new HashSet<>();
        Set<String> oldTokens = new HashSet<>();
        changedSection.stream().forEach(section -> {
            Arrays.stream(section.NEW.split("[^a-zA-Z0-9_]"))
                    .filter(token -> token.length() > 0)
                    .forEach(token -> newTokens.add(token));

            Arrays.stream(section.OLD.split("[^a-zA-Z0-9_]"))
                    .filter(token -> token.length() > 0)
                    .forEach(token -> oldTokens.add(token));
        });

        tokens = new Change<>(
                SetTool.difference(newTokens, oldTokens),
                SetTool.difference(oldTokens, newTokens)
        );
    }

    /**
     * 用于解析代码前后版本出现的不一致的token
     * @param diff
     */
    private void extractWords(Diff diff) {
        if (getName().equals("BKDReader")) {
            int a = 2;
        }
        SetTool.union(diff.delFields.keySet(), diff.delMethods.keySet()).stream()
                .filter(token -> !tokens.NEW.contains(token))
                .filter(token -> tokens.OLD.contains(token))
                .forEach(token -> delWords.add(token));

        SetTool.union(diff.newFields.keySet(), diff.newMethods.keySet()).stream()
                .filter(token -> tokens.NEW.contains(token))
                .filter(token -> !tokens.OLD.contains(token))
                .forEach(token -> addWords.add(token));

        for (String methodName: diff.changedMethods.keySet()) {
            diff.changedMethods.get(methodName).stream()
                    .forEach(method -> {
                        if (tokens.NEW.contains(method.name.NEW)
                                && tokens.OLD.contains(method.name.OLD)) {
                            changedWords.add(method.name.NEW);
                            changedWords.add(method.name.OLD);
                        }
                    });
        }

        for (String filedName: diff.changedFields.keySet()) {
            diff.changedFields.get(filedName).stream()
                    .forEach(field -> {
                        if (tokens.NEW.contains(field.name.NEW ) &&
                                tokens.OLD.contains(field.name.OLD)) {
                            changedWords.add(field.name.NEW);
                            changedWords.add(field.name.OLD);
                        }
                    });
        }
        changedWords.add(getName());
        changedWords.add(file.getName());
    }

    @Override
    public String getName() {
        return name.NEW;
    }

    /**
     * MethodDiff首先在初始化只能考虑到当前函数的内容的变化，而无法考虑到全局的，也就是整个一次commit的信息
     * 因此，update传入一个参数diff，diff记录了一次commit中的一些重要信息
     * 用传入的diff来用全局的信息来更新MethodDiff
     * @param diff
     */
    @Override
    public void fieldUpdate(Diff diff) {
        extractWords(diff);

    }

    public boolean isRelated(Comment comment) {
        for (String word: delWords)
            if (comment.content.contains(word))
                return true;
        for (String word: addWords)
            if (comment.content.contains(word))
                return true;

        for (String word: changedWords)
            if (comment.content.contains(word))
                return true;
        return false;
    }

    public String highlighter(String content) {
        for (String word: delWords)
            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
        for (String word: addWords)
            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
        for (String word: changedWords)
            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
        if (content.contains(name.NEW))
            content = content.replaceAll(name.NEW, "&nbsp;<strong>"+ name.NEW + "</strong>&nbsp;");
        return content;
    }

    @Override
    public void update(Diff diff) {
        if (name.NEW.equals("getIntersectState")) {
            int a = 2;
        }
        fieldUpdate(diff);
        getStaticModifies(diff);
    }

    @Override
    public String toString() {
        return name.NEW;
    }

    public Change<List<String>> getVariables() {
        Change<List<String>> result = new Change<List<String>>(new ArrayList<>(), new ArrayList<>());
        node.NEW.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName node) {
                result.NEW.add(node.toString());
                return false;
            }

            public boolean visit(SimpleType node) {
                return false;
            }
        });

        node.OLD.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName node) {
                result.OLD.add(node.toString());
                return false;
            }

            public boolean visit(SimpleType node) {
                return false;
            }
        });
        return null;
    }

    /**
     * 该函数用于在获取一段
     * @return
     */
    public void getStaticModifies(Diff diff) {
        getFieldModifies(diff);
        getMethodModifies(diff);
    }

    public void getFieldModifies(Diff diff) {
        if (!(file instanceof ChangedClass)) return;
        ChangedClass clazz = (ChangedClass) file;
        for (Change token : changedFields) {
            for (ChangedField field : clazz.changedFields) {
                if (field.name.OLD.equals(token.OLD) && field.name.NEW.equals(token.NEW)) {
                    FieldReplace fr = new FieldReplace(field.name.OLD, field.comment.OLD, field.name.NEW, field.comment.NEW);
                    modifies.add(fr);
                    MethodDiff.connect(field, this);
                }
            }
        }
    }

    public void getMethodModifies(Diff diff) {
        for (SourceCodeChange change: sourceCodeChanges) {
            if (change instanceof Update && change.getChangeType() == ChangeType.STATEMENT_UPDATE) {
                Update update = (Update) change;
                Set<String> tokensInOld = SetTool.toSet(change.getChangedEntity().getUniqueName());
                Set<String> tokensInNew = SetTool.toSet(update.getNewEntity().getUniqueName());

                Set<String> set1 = SetTool.difference(tokensInOld, tokensInNew);
                Set<String> set2 = SetTool.insert(tokensInNew, tokensInOld);
                Set<String> set3 = SetTool.difference(tokensInNew, tokensInOld);

                set2.stream().filter(token -> diff.changedMethods.containsKey(token))
                        .forEach(token -> {
                            ChangedMethod method = diff.changedMethods.get(token).iterator().next();
                            MethodReplace replace = new MethodReplace(method.name.OLD, method.comment.OLD,
                                    method.name.NEW, method.comment.NEW);
                            modifies.add(replace);
                        });

            }
        }
    }

}
