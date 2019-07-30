package fileDiff.file;

import description.Description;
import fileDiff.Diff;
import fileDiff.method.MethodDiff;
import fileDiff.field.FieldDiff;
import fileDiff.rationale.Explainable;
import fileDiff.type.FileType;
import javassist.runtime.Desc;
import sun.security.krb5.internal.crypto.Des;
import util.SetTool;
import util.StemTool;

import java.io.Serializable;
import java.util.*;

/**
 * Created by kvirus on 2019/4/20 18:53
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class FileDiff implements Explainable, Serializable {
    public FileType type;

    public String commitId = "";

    public Diff diff;

    protected HashSet<String> changedFiledNames = null;

    protected HashSet<String> changedMethodNames = null;

    /**
     * descriptions记录了从issue等来源与当前类相关的描述信息。
     */
    public List<Description> descriptions = new ArrayList<>();

    public abstract HashSet<String> getChangedFiledNames();

    public abstract HashSet<String> getChangedMethodNames();

    public abstract String getName();

    public abstract String getPath();

    public abstract List<FieldDiff> getFields();

    public abstract List<MethodDiff> getMethods();

    public abstract List<FieldDiff> getFields(String fieldName, boolean withComment);

    public abstract List<MethodDiff> getMethods(String methodName, boolean withComment);

    /**
     * 该函数用于发现当前文件的与之前一个版本的差异中，是否出现阈值的删除、增加
     * 删除的field，存储在delFields中
     * 增加的field，存储在addFields中
     */
    public abstract void fieldDiff();

    public abstract void methodDiff();

    public abstract void parse();

    public void print() {
        printFields();
        printMethods();
    }

    protected abstract void printFields();

    protected abstract void printMethods();

    public void update(Diff diff) {
        ;
    }

    public abstract String toString();

    @Override
    public void matchDescription(List<Description> desList) {
        Map<String, Set<Description>> method2Des = new HashMap<>();
        Map<String, Set<Description>> field2Des = new HashMap<>();
        for (Description des: desList) {
            for (String methodName: des.methods)
                SetTool.add(method2Des, methodName, des);

            for (String field: des.fields)
                SetTool.add(field2Des, field, des);
        }

        Map<String, Set<MethodDiff>> method2Met = new HashMap<>();
        Map<String, Set<MethodDiff>> field2Met = new HashMap<>();
        for (MethodDiff method: this.getMethods()) {
            String methodName = method.getName();
            SetTool.add(method2Met, methodName, method);
            for (String token: SetTool.union(method.changedWords, method.addWords, method.delWords)) {
                SetTool.add(field2Met, methodName, method);
            }
        }

        for (String methodName: method2Des.keySet()) {
            if (!method2Met.containsKey(methodName)) continue;
            for (MethodDiff m: method2Met.get(methodName))
                m.descriptions.addAll(method2Des.get(methodName));
        }

        for (String fieldName: field2Des.keySet()) {
            if (!field2Met.containsKey(fieldName)) continue;
            for (MethodDiff m: field2Met.get(fieldName))
                m.descriptions.addAll(field2Des.get(fieldName));
        }

        /*matchDescriptionBasedOnMethodName(desList);
        matchDescriptionBasedOnKeyWords(desList);*/
    }

    private void matchDescriptionBasedOnMethodName(List<Description> desList) {
        Map<String, List<MethodDiff>> methods = new HashMap<>();
        for (MethodDiff method: getMethods()) {
            String methodName = method.getName();
            if (!methods.containsKey(methodName)) methods.put(methodName, new ArrayList<>());
            methods.get(methodName).add(method);
        }

        desList.removeIf(des -> {
            boolean s = false;
            for (String methodName: methods.keySet()) {
                if (des.methods.contains(methodName)) {
                    for (MethodDiff method: methods.get(methodName)) {
                        method.descriptions.add(des);
                    }
                    s = true;
                    break;
                }
            }
            return s;
        });
    }

    private void matchDescriptionBasedOnKeyWords(List<Description> desList){
        //倒排索引
        Map<String, Set<MethodDiff>> inverseIndex = new HashMap<>();
        for (MethodDiff method: getMethods()) {
            List<String> words = new ArrayList<>();
            words.addAll(method.addWords);
            words.addAll(method.delWords);
            words.addAll(method.changedWords);
            for (String word: words) {
                word = StemTool.stemSingleWord(word);
                if (!(inverseIndex.containsKey(word))) {
                    inverseIndex.put(word, new HashSet<>());
                }
                inverseIndex.get(word).add(method);
            }
        }

        String res = "[^a-zA-Z0-9_-]";
        Map<Description, Set<String>> tokens = new HashMap<>();
        for (Description des: desList) {
            HashSet<String> tokenSet = new HashSet<>(StemTool.stem(des.description));
            tokens.put(des, tokenSet);
        }

        //获取每个token的出现次数
        Map<String, Integer> tokenCount = new HashMap<>();
        for (Description des: tokens.keySet()) {
            for (String token: tokens.get(des)) {
                tokenCount.put(token, tokenCount.getOrDefault(token, 0) + 1);
            }
        }

        desList.removeIf(des -> {
            if (des.description.contains("version")) {
                int a = 2;
            }

            boolean s = false;
            List<String> tokenList = new ArrayList<>(tokens.get(des));
            tokenList.sort(Comparator.comparingInt(token -> tokenCount.get(token)));

            for (String token: tokenList) {
                if (inverseIndex.containsKey(token)) {
                    for (MethodDiff method: inverseIndex.get(token)) {
                        method.descriptions.add(des);
                    }
                    s = true;
                    break;
                }
            }
            return s;
        });
    }
}
