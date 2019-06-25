package fileDiff.modify.type;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.signatureChange.ParameterInsert;
import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import org.reflections.Reflections;
import static org.reflections.ReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by kvirus on 2019/4/23 22:29
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */

public abstract class Modify {
    //public static Map<Class, Modify> classes = new HashMap<>();
    public static List<Pair<Class<? extends Modify>, Modify>> classes = new ArrayList<>();
    static {
        Reflections reflections = new Reflections("fileDiff.modify.type");
        Set<Class<? extends Modify>> subTypes = reflections.getSubTypesOf(Modify.class);
        for (Class clazz: subTypes) {
            try {
                classes.add(new Pair<Class<? extends Modify>, Modify>(clazz, (Modify)clazz.newInstance()));
            } catch (Exception e) {
                ;
            }
        }
        classes.sort(Comparator.comparingInt(p -> p.getKey().getAnnotation(Order.class).order().order));

    }


    protected StringBuilder content = new StringBuilder();

    protected StringBuilder extension = new StringBuilder();

    protected abstract void build();

    public String getContent() {
        String result = content.toString();
        if (extension.length() > 0)
            result += ("\n") + extension;
        return result;
    }

    public void extend(String str) {
        if (extension.length() > 0)
            extension.append("\n");
        extension.append(str);
    }

    public abstract boolean equals(Object obj);

    public static Modify match(MethodDiff method) {
        Modify result = null;
        for (Pair<Class<? extends Modify>, Modify> p: classes) {
            try {
                result = (Modify)p.getKey().getMethod("match", MethodDiff.class).invoke(p.getValue(), method);
                if (result != null) break;
            } catch (Exception e) {
                System.out.println(p.getKey().getSimpleName());
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Result match(GitAnalyzer git, RevCommit commit) {
        System.out.println(commit.getId().toString());

        Map<String, List<MethodDiff>> matchResult = new HashMap<>();
        Result result = new Result();
        Diff diff = new Diff(git, commit.getId());
        diff.getClasses().stream().filter(file -> !file.getName().toLowerCase().contains("test"))
            .flatMap(file -> file.getMethods().stream())
            .filter(methodDiff -> {
                boolean s = methodDiff instanceof ChangedMethod;
                if (s) result.changedMethods ++;
                result.allMethods ++;
                return s;
            })
            .forEach(methodDiff -> {
                Modify modify = Modify.match(methodDiff);
                String matchType = "";
                if(modify != null) {
                    result.classified ++;
                    matchType = modify.getClass().getSimpleName();
                } else {
                    matchType = "null";
                }
                if (!( matchResult.containsKey(matchType) )) matchResult.put(matchType, new ArrayList<>());
                matchResult.get(matchType).add(methodDiff);
            });

        /*for (String type: matchResult.keySet()) {

            List<MethodDiff> methods = matchResult.get(type);
            System.out.println(type + ": " + methods.size());
            methods.sort((m1, m2) -> {
                String s1 = m1.file.getName() + "." + m1.getName();
                String s2 = m2.file.getName() + "." + m2.getName();
                return s1.compareTo(s2);
            });
            for (MethodDiff m: methods)
                System.out.println(" " + m.file.getName() + "." + m.getName());
        }*/

        result.print();
        return result;
    }

    public static void test() {
        Result result = new Result();
        GitAnalyzer git = new GitAnalyzer();
        List<RevCommit> commits = git.getCommits();
        for (int i = 0; i < commits.size(); i++) {
            try {
                Result r = match(git, commits.get(i));
                //Result r = match(git, git.getCommit("1118299c338253cea09640acdc48dc930dc27fda"));
                result.add(r);

                if (i % 200 == 0) {
                    System.out.print(i + ":");
                    result.print();
                }
            } catch (Exception e) {
                System.out.println(commits.get(i).toString() + ": error" );
            }
        }
        result.print();
        print();
    }

    public static void print() {
        for (Pair<Class<? extends Modify>, Modify> p: classes) {
            try {
                System.out.println(p.getKey().getName() + ": " + p.getKey().getField("count").toString());
            } catch (Exception e) {
                System.out.println(p.getKey().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}

