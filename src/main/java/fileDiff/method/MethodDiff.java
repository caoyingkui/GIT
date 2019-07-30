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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import util.CompileTool;
import util.SetTool;
import util.StemTool;

import javax.validation.constraints.NotNull;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
public abstract class MethodDiff implements Update, Explainable, Serializable {
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

    public static List<StatementHash> getHashes(String newContent, String oldContent) {

        List<SourceCodeChange> changes = getChanges(newContent, oldContent);

        changes.removeIf(c -> {
            EntityType type = c.getChangedEntity().getType();
            return type == JavaEntityType.BLOCK_COMMENT || type == JavaEntityType.LINE_COMMENT || type == JavaEntityType.JAVADOC;
        });
        List<StatementHash> hashes = new ArrayList<>();
//        MethodDeclaration newDec = CompileTool.getMethodFromClass(newContent);
//        MethodDeclaration oldDec = CompileTool.getMethodFromClass(oldContent);

        try {
            MethodDeclaration newDec = CompileTool.getMethodFromMethod(newContent);
            MethodDeclaration oldDec = CompileTool.getMethodFromMethod(oldContent);

            for (SourceCodeChange change : changes)
                hashes.add(StatementHash.getInstance(change, newDec, oldDec));
        } catch (Exception e) {
            MethodDeclaration newDec = CompileTool.getMethodFromMethod(newContent);
            MethodDeclaration oldDec = CompileTool.getMethodFromMethod(oldContent);
            e.printStackTrace();
        }
        return hashes;
    }

    public static int isSimilar(Change<String> method1, Change<String> method2) {
        List<StatementHash> hashes1 = getHashes(method1.NEW, method1.OLD);
        List<StatementHash> hashes2 = getHashes(method2.NEW, method2.OLD);
        return isSimilar(hashes1, hashes2);
    }

    public static int isSimilar1(List<StatementHash> hashes1, List<StatementHash> hashes2) {
        int count = 0;
        List<StatementHash> big = null, small = null;
        if (hashes1.size( ) == 0 || hashes2.size() == 0) return 0;
        if (hashes1.size() > hashes2.size()) {
            big = hashes1;
            small = hashes2;
        } else {
            big = hashes2;
            small = hashes1;
        }
        Set<Integer> set1 = new HashSet<>();
        Set<Integer> set2 = new HashSet<>();
        for (StatementHash h1: small) {
            for (int i = 0; i < big.size(); i++) {

                StatementHash h2 = big.get(i);
            //for (StatementHash h2: big) {
                try {
                    if (h1.equals(h2)) {
                        if (!set1.contains(i)) {
                            count++;
                            set1.add(i);
                            break;
                        }
                        //break;
                    }
                } catch (Exception e) {
                    int a = 2;
                }
            }
        }

        int min = Math.min(hashes1.size(), hashes2.size());
        //return count == min || (min > 10 && count > min * 0.75);
        return small.size() / 0.75 >= big.size() && (set1.size() >= 0.75 * min || set1.size() > 1) ? set1.size() : -set1.size();
    }

    public static int isSimilar2(List<StatementHash> hashes1, List<StatementHash> hashes2) {
        int size1 = hashes1.size(), size2 = hashes2.size();
        int arr1[] = new int[size1 + 2], arr2[] = new int[size2 + 2];
        for (int i = 0; i < size1 + 2; i++) arr1[i] = 0;
        for (int i = 0; i < size2 + 2; i++) arr2[i] = 0;

        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                if (hashes1.get(i).equals(hashes2.get(j))) {
                    arr2[j + 1] = i + 1;
                    arr1[i + 1] = j + 1;
                }
            }
        }

        int count1 = 0, count2 = 0, temp = 0;
        for (int i = 1; i <= size1; i++) {
            if (arr1[i] > arr1[i - 1]) temp ++;
            else {
                count1 = Math.max(count1, temp);
                temp = arr1[i] > 0 ? 1 : 0;
            }
        }

        temp = 0;
        for (int i = 1; i <= size2; i++) {
            if (arr2[i] > arr2[i - 1]) temp ++;
            else {
                count2 = Math.max(count2, temp);
                temp = arr2[i] == 0 ? 0 : 1;
            }
        }

        int max = Math.max(count1, count2);
        int larger = Math.max(size1, size2), smaller = Math.min(size1, size2);
        boolean condition1 = max > 1 && max >= 0.5 * smaller;
        return condition1 ? max : -max;
    }

    public static int isSimilar(List<StatementHash> hashes1, List<StatementHash> hashes2) {
        int size1 = hashes1.size(), size2 = hashes2.size();
        int matrix[][] = new int[size1][size2];
        Set<Integer> similarSet1 = new HashSet<>();
        Set<Integer> similarSet2 = new HashSet<>();

        // statementhash一种有4种类型，增、删、改、移
        Set<Integer> typeFrom1 = new HashSet<>(); // 记录第一个hash中的类型
        Set<Integer> typeFrom2 = new HashSet<>(); // 记录第二个hash中的类型
        Set<Integer> typeSame  = new HashSet<>(); // 记录相同的hash的类型

        hashes1.forEach(hash -> typeFrom1.add(hash.hashes[0]));
        hashes2.forEach(hash -> typeFrom2.add(hash.hashes[0]));


        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                if (hashes1.get(i).equals(hashes2.get(j))) {
                    typeSame.add(hashes1.get(i).hashes[0]);
                    matrix[i][j] = 1;
                    similarSet1.add(i);
                    similarSet2.add(j);
                }
                else matrix[i][j] = 0;
            }
        }

        boolean condition0 = true;
        for (int type: typeFrom1) {
            if (!typeSame.contains(type)) {
                condition0 = false;
                break;
            }
        }

        for (int type: typeFrom2) {
            if (!typeSame.contains(type)) {
                condition0 = false;
                break;
            }
        }

        int similarSize = Math.min(similarSet1.size(), similarSet2.size());

        int max = 0, temp;
        for (int i = -size1 + 1; i < size2; i++) {
            temp = 0;
            for (int j = Math.max(0 - i, 0); j < size1; j++) {
                if (i + j >= size2) break;
                if (matrix[j][i + j] == 1) {
                    temp++;
                } else {
                    if (temp > max) max = temp;
                    temp = 0;
                }
            }
            if (temp > max) max = temp;
        }
        //return max > 1 ? max : 0;

        int larger = Math.max(size1, size2), smaller = Math.min(size1, size2);

        boolean condition1 = max > 1 && max >= 0.3 * smaller && smaller >= 0.3 * larger;
        boolean condition2 = (similarSize >= 3 || similarSize >= 2 && smaller <= 3) ;
        //boolean condition3 = similarSize == smaller;
        boolean condition3 = larger == smaller && max == smaller;
        boolean condition4 = similarSize > 5 ;
        boolean condition5 = smaller >= 0.5 * larger;
        boolean condition6 = max >= 2 && max == smaller || larger <= 3 && similarSize == smaller;
        boolean condition7 = similarSize >= 0.5 * smaller;
        if (condition4) return similarSize;
        return   (condition7 && ( condition2 || condition3 || condition4) && (condition1 || condition5) || condition6) ? Math.max(max, similarSize) : - Math.max(max, similarSize);

    }
}


