package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.method.NewMethod;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import fileDiff.modify.type.ifStatement.BranchInsert;
import fileDiff.modify.type.ifStatement.ConditionChange;
import fileDiff.modify.type.ifStatement.ConditionDelete;
import fileDiff.modify.type.ifStatement.ConditionInsert;
import fileDiff.modify.type.javadoc.JavaDocInsert;
import fileDiff.modify.type.javadoc.JavadocDelete;
import fileDiff.modify.type.javadoc.JavadocUpdate;
import fileDiff.modify.type.statementChange.FieldChange;
import fileDiff.modify.type.statementChange.MethodChange;
import fileDiff.modify.type.statementChange.NewClassImport;
import git.GitAnalyzer;
import org.eclipse.jgit.revwalk.RevCommit;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import static org.reflections.ReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by kvirus on 2019/5/18 20:12
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ParameterDelete extends Modify {
    public static int count = 0;
    List<String> deleteParameters = new ArrayList<>();

    public static ParameterDelete match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        ParameterDelete result = new ParameterDelete();
        boolean s = false;
        ChangedMethod cMethod = (ChangedMethod) method;
        cMethod.getSourceCodeChanges().stream().filter(change -> change.getChangeType() == ChangeType.PARAMETER_DELETE)
                .forEach(change -> {
                    result.deleteParameters.add(change.getChangedEntity().getUniqueName());
                });

        for (SourceCodeChange change: cMethod.getSourceCodeChanges()) {
            String str = "";
            if (change instanceof Delete) {
                str = change.getChangedEntity().getUniqueName();
            } else if (change instanceof Update) {
                str = change.getChangedEntity().getUniqueName();
            }
            boolean sig = false;
            for (String token: result.deleteParameters) {
                if (str.contains(token)) {
                    sig = true;
                    break;
                }
            }
            if (!sig) return null;
        }
        return result.deleteParameters.size() > 0 && (++count) > 0 ? result : null;
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

    public static void main(String[] args) {
        count();
        /*GitAnalyzer git = new GitAnalyzer();
        Diff d = new Diff(git, git.getId("55bfadbce115a825a75686fe0bfe71406bc3ee44"));
        FileDiff c = null;
        String targetName = "lucene/core/src/java/org/apache/lucene/search/TopFieldCollector.java";
        targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
        //targetName = "MultiComparatorLeafCollector";
        String methodName = "PagingFieldCollector";
        for (FileDiff file: d.getClasses()) {
            if (file.getName().equals(targetName)) {
                c = file;
                break;
            }
        }

        for (MethodDiff method: c.getMethods()) {
            if (method.getName().equals(methodName)) {
                ParameterDelete result = ParameterDelete.match(method);
                int a = 2;
            }
        }*/
    }

    public static void count() {
        GitAnalyzer git = new GitAnalyzer();
        List<RevCommit> commits = git.getCommits();
        int length = commits.size();
        Set<Integer> visit = new HashSet<>();
        int total = 0, ans = 0, all = 0;
        int t1 = 0, t2 = 0, t3 = 0;
        for (int i = 0; i < commits.size();) {
            try {
                Random ra = new Random();
                int index = -1;
                do {
                    index = i;
                    //index = ra.nextInt(length);
                } while (visit.contains(index));
                visit.add(index);
                i++;
                System.out.println(commits.get(index).getId().toString());
                Diff diff = new Diff(git, commits.get(index).getId());
                //Diff diff = new Diff(git, git.getId("1118299c338253cea09640acdc48dc930dc27fda"));
                ans = total = all = 0;
                for (FileDiff file : diff.getClasses()) {
                    if (file.getName().toLowerCase().contains("test")) continue;
                    for (MethodDiff method : file.getMethods()) {
                        all ++;
                        if (!(method instanceof ChangedMethod)) continue;

                        Reflections reflections = new Reflections("fileDiff.modify.type");
                        Set<Class<? extends Modify>> subTypes = reflections.getSubTypesOf(Modify.class);

                        Map<Class, Method> methods = new HashMap<>();
                        for (Class clazz: subTypes) {
                            methods.put(clazz, ReflectionUtils.getAllMethods(clazz, withName("match")).iterator().next());
                        }

                        for (Class clazz: methods.keySet()) {
                            MethodDiff res = (MethodDiff) (methods.get(clazz).invoke(clazz, method));
                            if (res != null) {
                                System.out.println(clazz.getName() + " " + method.getName() + ": " + res.getName());
                                ans ++;
                            }
                        }

                        System.out.println(file.getName() + "." + method.getName());
                        if (BranchInsert.match(method) != null) ans ++;
                        else if (ConditionChange.match(method) != null) ans ++;
                        else if (ConditionDelete.match(method) != null) ans ++;
                        else if (ConditionInsert.match(method) != null) ans ++;
                        else if (ParameterDelete.match(method) != null) ans++;
                        else if (ParameterInsert.match(method) != null) ans++;
                        else if (ParameterModify.match(method) != null) ans ++;
                        else if (ParameterRename.match(method) != null) ans ++;
                        else if (Rename.match(method) != null) ans ++;
                        else if (ReturnTypeChange.match(method) != null) ans++;
                        else if (ReturnTypeDelete.match(method) != null) ans++;
                        else if (ReturnTypeInsert.match(method) != null) ans++;
                        else if (FieldChange.match(method) != null) ans++;
                        else if (MethodChange.match(method) != null) ans++;
                        else if (NewClassImport.match(method) != null) ans++;
                        else if (JavadocDelete.match(method) != null) ans ++;
                        else if (JavaDocInsert.match(method) != null) ans ++;
                        else if (JavadocUpdate.match(method) != null) ans ++;
                        //else if (method instanceof ChangedMethod)
                            //System.out.println(((ChangedMethod) method).fullName.NEW);
                        total++;
                    }
                }
                t1 += ans;
                t2 += total;
                t3 += all;
                System.out.println(ans + " in " + total + " "  + all);
            } catch (Exception e) {
                continue;
            }

            if(i % 200 == 0) {
                System.out.println(i);
                System.out.println("cyk: " + t1 + " in " + t2 + " " + t3);
                System.out.println(BranchInsert.class.toString() + " " + BranchInsert.count);
                System.out.println(ConditionChange.class.toString() + " " + ConditionChange.count);
                System.out.println(ConditionDelete.class.toString() + " " + ConditionDelete.count);
                System.out.println(ConditionInsert.class.toString() + " " + ConditionInsert.count);
                System.out.println(JavadocDelete.class.toString() + " " + JavadocDelete.count);
                System.out.println(JavaDocInsert.class.toString() + " " + JavaDocInsert.count);
                System.out.println(JavadocUpdate.class.toString() + " " + JavadocUpdate.count);
                System.out.println(ParameterDelete.class.toString() + " " + ParameterDelete.count);
                System.out.println(ParameterInsert.class.toString() + " " + ParameterInsert.count);
                System.out.println(ParameterModify.class.toString() + " " + ParameterModify.count);
                System.out.println(ParameterRename.class.toString() + " " + ParameterRename.count);
                System.out.println(Rename.class.toString() + " " + Rename.count);
                System.out.println(ReturnTypeDelete.class.toString() + " " + ReturnTypeDelete.count);
                System.out.println(ReturnTypeInsert.class.toString() + " " + ReturnTypeInsert.count);
                System.out.println(ReturnTypeChange.class.toString() + " " + ReturnTypeChange.count);
                System.out.println(FieldChange.class.toString() + " " + FieldChange.count);
                System.out.println(MethodChange.class.toString() + " " + MethodChange.count);
                System.out.println(NewClassImport.class.toString() + " " + NewClassImport.count);
            }
            //break;
        }
        System.out.println(t1 + " in " + t2 + " " + t3);

        System.out.println(BranchInsert.class.toString() + " " + BranchInsert.count);
        System.out.println(ConditionChange.class.toString() + " " + ConditionChange.count);
        System.out.println(ConditionDelete.class.toString() + " " + ConditionDelete.count);
        System.out.println(ConditionInsert.class.toString() + " " + ConditionInsert.count);
        System.out.println(JavadocDelete.class.toString() + " " + JavadocDelete.count);
        System.out.println(JavaDocInsert.class.toString() + " " + JavaDocInsert.count);
        System.out.println(JavadocUpdate.class.toString() + " " + JavadocUpdate.count);
        System.out.println(ParameterDelete.class.toString() + " " + ParameterDelete.count);
        System.out.println(ParameterInsert.class.toString() + " " + ParameterInsert.count);
        System.out.println(ParameterModify.class.toString() + " " + ParameterModify.count);
        System.out.println(ParameterRename.class.toString() + " " + ParameterRename.count);
        System.out.println(Rename.class.toString() + " " + Rename.count);
        System.out.println(ReturnTypeDelete.class.toString() + " " + ReturnTypeDelete.count);
        System.out.println(ReturnTypeInsert.class.toString() + " " + ReturnTypeInsert.count);
        System.out.println(ReturnTypeChange.class.toString() + " " + ReturnTypeChange.count);
        System.out.println(FieldChange.class.toString() + " " + FieldChange.count);
        System.out.println(MethodChange.class.toString() + " " + MethodChange.count);
        System.out.println(NewClassImport.class.toString() + " " + NewClassImport.count);
    }

}
