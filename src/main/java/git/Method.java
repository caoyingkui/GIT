package git;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import util.ReaderTool;

import java.util.Arrays;
import java.util.List;

public class Method {
    public String           fullName ;
    public String           name;
    public int              startLine;
    public int              endLine;
    public int              startPos;
    public int              endPos;
    public String           methodContent;
    public String           comment;
    public MethodDeclaration node;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public void setMethodContent(String methodContent) {
        this.methodContent = methodContent;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setNode(MethodDeclaration node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Method))
            return false;
        Method term = (Method) obj;
        return fullName.equals(term.fullName);
    }

    public static boolean isIdentical(Method method1, Method method2){
        return method1.methodContent.equals(method2.methodContent);
    }

    /**
     * 该方法用于发现在oldMethods集合中是有存在和method相似的函数
     * 主要考虑的是，在一个文件前后提交的版本中，如果一个函数的发生了函数名上的重构，
     * 那么就没有办法被直接定义为新增函数或删除函数
     * 因此当发现当前版本存在一个新增的函数名时，就要首先判断是否在上一个版本从存在类似的函数
     * 判断函数是不是相似，就要看看他们的内容中有没有连续相同的代码。
     * @param method
     * @param oldMethods
     * @return
     */
    public static int findSimilarCandidate(Method method, List<Method> oldMethods) {
        List<String> parameters = getParameters(method.fullName);
        int maxCount = 0, index = -1; // 记录参数最多的相似个数
        for (int i = 0; i < oldMethods.size(); i++) {
            Method old = oldMethods.get(i);
            List<String> ps = getParameters(old.fullName);

            int temp = method.name.equals(old.name) ? 100 : 0;
            for (int j = 0; j < parameters.size(); j++) {
                String p = parameters.get(j);
                for (int k = 0; k < ps.size(); k++) {
                    if (p.equals(ps.get(k))) {
                        temp++;
                        ps.set(k, "");
                    }
                }
            }

            if (temp > maxCount && ( temp > 100 || Method.isSimilar(method, old))) {
                maxCount = temp;
                index = i;
            }

        }
        return index;
    }

    static List<String> getParameters(String methodFullName) {
        try {
            String parameters = methodFullName.substring(methodFullName.indexOf('(') + 1, methodFullName.lastIndexOf(')'));
            String[] paras = parameters.split(",", 0);
            return Arrays.asList(paras);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSimilar(Method method1, Method method2) {
        return isSimilar(method1.methodContent, method2.methodContent);
    }

    public static boolean isSimilar(String content1, String content2) {
        EditList editList = GitAnalyzer.getEditList(content1, content2);

        int index = -1, total = 0, difference = 0;
        // 计算两段代码的总行数
        while ((index = content1.indexOf('\n', ++index)) > -1)
            total ++;
        index = -1;
        while ((index = content1.indexOf('\n', ++index ) )> - 1)
            total ++;

        // 计算两段代码中不一致的函数
        for (Edit edit: editList) {
            difference += (edit.getEndA() - edit.getEndB());
            difference += (edit.getEndB() - edit.getBeginB());
        }
        return difference > (total / 3);
    }

    public String toString() {
        return name;
    }

    public static void main(String[] args) {
        String content1 = ReaderTool.read("file1.java");
        String content2 = ReaderTool.read("file2.java");
        isSimilar(content1, content2);
    }
}
