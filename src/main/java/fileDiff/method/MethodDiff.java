package fileDiff.method;

import analyzer.histories.Comment;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import description.Description;
import fileDiff.Diff;
import fileDiff.field.FieldDiff;
import fileDiff.file.FileDiff;
import fileDiff.rationale.Explainable;
import util.SetTool;
import util.StemTool;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by kvirus on 2019/4/20 16:45
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class MethodDiff implements Update, Explainable {
    public String commitId;

    @NotNull
    public FileDiff file = null;

    public int start;

    public int length;

    //记录该函数由哪些域的改动，而受到影响
    public List<FieldDiff> affectedByField = null;

    //记录该函数由哪些方法的改动，而受到影响
    public List<MethodDiff> affectedByMethod = null;

    //记录该函数的改动，会影响到哪些函数
    public List<MethodDiff> affectMethod = null;

    //delWords、addWords和changedWords记录的是前后发生变化的filed、method等重要信息。
    public Set<String> delWords = new HashSet<>();
    public Set<String> addWords = new HashSet<>();
    public Set<String> changedWords = new HashSet<>();

    public List<Description> descriptions = new ArrayList<>();

    /**
     * 查看一下methodDiff中和comment有多少token相同
     * @param comment
     * @return
     */
    public abstract int commentWords(Comment comment);

    /**
     * 查看一下methodDiff中和comment有多少关键字相同
     * @param comment
     * @return
     */
    public abstract int commonKeyWords(Comment comment);

    protected abstract void extractChangedTokens();

    public abstract String getName();

    public String highlighter(String content) {
//        for (String word: delWords)
//            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
//        for (String word: addWords)
//            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
//        for (String word: changedWords)
//            content = content.replaceAll(word, "&nbsp;<strong>"+ word + "</strong>&nbsp;");
//        if (content.contains(name.NEW))
//            content = content.replaceAll(name.NEW, "&nbsp;<strong>"+ name.NEW + "</strong>&nbsp;");
//        return content;
        return "";
    }

    public static HashSet<String> extractTokens(String content) {
        HashSet<String> set = new HashSet<>();
        Arrays.stream(content.split("[^a-zA-Z0-9_]")).forEach(token -> set.add(token));
        return set;
    }

    protected static String getSignature(@NotNull String fullName) {
        int index = fullName.lastIndexOf(":");
        return index > -1 ? fullName.substring(index + 1) : "";
    }

    public abstract String toString();

    public static void connect(FieldDiff field, MethodDiff method) {
        if (field.affectedMethods == null) field.affectedMethods = new ArrayList<>();
        field.affectedMethods.add(method);

        if (method.affectedByField == null) method.affectedByField = new ArrayList<>();
        method.affectedByField.add(field);
    }

    public static void connect(MethodDiff initMethod, MethodDiff affectedMethod) {
        if (initMethod.affectMethod == null) initMethod.affectMethod = new ArrayList<>();
        initMethod.affectMethod.add(affectedMethod);

        if (affectedMethod.affectedByMethod == null) affectedMethod.affectedByMethod = new ArrayList<>();
        affectedMethod.affectedByMethod.add(initMethod);
    }

    @Override
    public void matchDescription(List<Description> desList) {
        if (this.getName().equals("BKDReader")) {
            int a = 2;
        }
        String methodName = getName();

        boolean s = false;
        if (this instanceof ChangedMethod) {
            for (SourceCodeChange change: ((ChangedMethod) this).getSourceCodeChanges()) {
                if (change instanceof Insert) {
                    s = true;
                    break;
                }
            }
        }
        if (!s) return;

        for (Description des: desList) {
            if (des.APIs.size() == 0) continue;
            if (des.description.contains("add version checking")) {
                int a = 2;
            }
            s = des.APIs.contains(methodName);
            if (!s) {
                for (String token: StemTool.camelCase(methodName)) {
                    if (des.stemmedTokens.contains(token)) {
                        s = true;
                        break;
                    }
                }
            }

            if (s ) {
                for (String api : des.APIs) {
                    //method没有包含某个API
                    if (!(delWords.contains(api) || addWords.contains(api) || changedWords.contains(api) || StemTool.stem(api).size() <= 1)) {
                        s = false;
                        break;
                    }
                }
                if (s) descriptions.add(des);
            }

            /*if (!s && (this instanceof ChangedMethod)) {
                ChangedMethod cm = (ChangedMethod) this;
                Set<String> tokens = SetTool.difference(SetTool.union(cm.tokens.NEW, cm.tokens.OLD), des.APIs);
                tokens.removeIf(token->StemTool.isStopWord(token.toLowerCase()));
                for (String token: tokens) {
                    if (des.tokens.contains(token)) {
                        descriptions.add(des);
                        break;
                    }
                }
            }*/
        }
    }

    public void matchDescription(Description des) {
        String methodName = getName();
        boolean s = false;
        if (des.description.contains(methodName)) {
            s = true;
        } else {
            List<String> tokens = StemTool.stem(des.description);
            for (String token: tokens) {
                if (des.tokens.contains(token)) {
                    s = true;
                    break;
                }
            }
        }

        if (!s) return;

        for (String API: des.APIs) {
            if( !(delWords.contains(API) || addWords.contains(API) || changedWords.contains(API)))
                return;
        }
        descriptions.add(des);
    }

    protected void getType() {
        String name = getName();
        String fileName = file.getName();
        //constructor
        if (name.equals(fileName)) {
            delWords.add("constructor");
        }
    }
}
