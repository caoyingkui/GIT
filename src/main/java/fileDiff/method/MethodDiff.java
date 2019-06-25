package fileDiff.method;

import analyzer.histories.Comment;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import description.Description;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.field.FieldDiff;
import fileDiff.file.FileDiff;
import fileDiff.group.hash.StatementHash;
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
    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    public Priority priority = Priority.LOW;

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

    public Set<Description> descriptions = new HashSet<>();

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
        String methodName = getName();
        if (methodName.equals("writeIndex")) {
            int a = 2;
        }

        desList.removeIf(des -> {
            if (des.methods.size() > 0) return !des.methods.contains(methodName);
            return true;
        });

        desList.forEach(des -> {
            if (des.fields.size() == 0) return;

            for (String field: des.fields) {
                if (addWords.contains(field)
                        || delWords.contains(field)
                        || changedWords.contains(field)) {
                    this.descriptions.add(des);
                }
            }
        });
    }

    protected void getType() {
        String name = getName();
        String fileName = file.getName();
        //constructor
        if (name.equals(fileName)) {
            //delWords.add("constructor");
        }
    }

    public static List<SourceCodeChange> getChanges(String newContent, String oldContent) {
        newContent = "class temp{" + newContent + "}";
        oldContent = "class temp{" + oldContent + "}";

        try {
            FileDistiller distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
            distiller.extractClassifiedSourceCodeChanges(oldContent, newContent, "");
            distiller.extractClassifiedSourceCodeChanges("", "");
            return distiller.getSourceCodeChanges();
        } catch (Exception e) {
            System.out.println("error");
            return new ArrayList<>();
        }
    }

    public static List<StatementHash> getHashes(List<SourceCodeChange> changes) {
        changes.removeIf(c -> {
            EntityType type = c.getChangedEntity().getType();
            return type == JavaEntityType.BLOCK_COMMENT || type == JavaEntityType.LINE_COMMENT;
        });

        List<StatementHash> hashes = new ArrayList<>();
        for (SourceCodeChange change: changes)
            hashes.add(StatementHash.getInstance(change));
        return hashes;
    }

    public static boolean isSimilar(Change<String> method1, Change<String> method2) {
        List<SourceCodeChange> changes1 = getChanges(method1.NEW, method1.OLD);
        List<SourceCodeChange> changes2 = getChanges(method2.NEW, method2.OLD);

        changes1.removeIf(c -> {
            EntityType type = c.getChangedEntity().getType();
            return type == JavaEntityType.BLOCK_COMMENT || type == JavaEntityType.LINE_COMMENT;
        });
        changes2.removeIf(c -> {
            EntityType type = c.getChangedEntity().getType();
            return type == JavaEntityType.BLOCK_COMMENT || type == JavaEntityType.LINE_COMMENT;
        });

        List<StatementHash> hashes1 = new ArrayList<>();
        List<StatementHash> hashes2 = new ArrayList<>();
        for (SourceCodeChange change: changes1)
            hashes1.add(StatementHash.getInstance(change));

        for (SourceCodeChange change: changes2)
            hashes2.add(StatementHash.getInstance(change));

        return isSimilar(hashes1, hashes2);
    }

    public static boolean isSimilar(List<StatementHash> hashes1, List<StatementHash> hashes2) {
        int count = 0;
        List<StatementHash> big = null, small = null;
        if (hashes1.size( ) == 0 || hashes2.size() == 0) return false;
        if (hashes1.size() > hashes2.size()) {
            big = hashes1;
            small = hashes2;
        } else {
            big = hashes2;
            small = hashes1;
        }
        for (StatementHash h1: small) {
            for (StatementHash h2: big) {
                try {
                    if (h1.equals(h2)) {
                        count++;
                        break;
                    }
                } catch (Exception e) {
                    int a = 2;
                }
            }
        }

        int min = Math.min(hashes1.size(), hashes2.size());
        return count == min || (min > 10 && count > min * 0.75);
    }
}


