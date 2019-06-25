package fileDiff.group;

import fileDiff.Diff;
import fileDiff.field.ChangedField;
import fileDiff.field.DelField;
import fileDiff.field.NewField;
import fileDiff.file.ChangedClass;
import fileDiff.file.DelClass;
import fileDiff.file.NewClass;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import util.SetTool;

import java.util.*;

/**
 * Created by kvirus on 2019/5/27 18:10
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class GroupSpliter {

    public static List<Group> split(Diff diff) {
        Set<String> tokens = new HashSet<>();
        List<MethodDiff> toCheck = new ArrayList<>();

        List<Group> result = new ArrayList<>();
        Group nullGroup = new Group(0, null);
        diff.getClasses().forEach(clazz -> {
            if (!( clazz.getName().toString().toLowerCase().contains("test") )) {
                tokens.addAll(clazz.getChangedMethodNames());
                tokens.addAll(clazz.getChangedFiledNames());
                for (MethodDiff m: clazz.getMethods()) {
                    if (m instanceof ChangedMethod) {
                        if (m.delWords.size() > 0 ||
                                m.addWords.size() > 0 ||
                                m.changedWords.size() > 0)
                            toCheck.add(m);
                        else
                            nullGroup.addMethod(m);
                    }
                }
            }
        });
        if (nullGroup.getMethods().size() > 0)
            result.add(nullGroup);

        while(true) {
            if (toCheck.size() == 0) break;
            Map<String, Integer> count = new HashMap<>();
            for (MethodDiff m: toCheck) {
                for (String token: SetTool.union(m.changedWords, m.addWords, m.delWords)) {
                    if (count.containsKey(token)) count.put(token, count.get(token) + 1);
                    else count.put(token, 1);
                }
            }

            int max = -1;
            String target = "";
            for (String token: count.keySet()) {
                if (count.get(token) > max) {
                    max = count.get(token);
                    target = token;
                }
            }

            HashSet<String> keyWords = new HashSet<>();
            keyWords.add(target);
            Group group = new Group(0, keyWords);
            final String t = target;
            toCheck.removeIf(m -> {
                if (m.changedWords.contains(t) ||
                    m.addWords.contains(t) ||
                    m.delWords.contains(t)) {
                    group.addMethod(m);

                    return true;
                } else {
                    return false;
                }
            });
            result.add(group);
        }

        return result;

    }

    public static HashSet<String> getFields(Diff diff) {
        HashSet<String> result = new HashSet<>();
        diff.getClasses().stream().forEach(clazz -> {
            result.addAll(clazz.getChangedFiledNames());
        });
        return result;
    }

    public static HashSet<String> getMethods(Diff diff) {
        HashSet<String> result = new HashSet<>();
        diff.getClasses().stream().forEach(clazz -> {
            result.addAll(clazz.getChangedMethodNames());
        });
        return result;
    }

    public static Pair<Integer, HashSet<String>> getHash(MethodDiff method, HashSet<String> tokens) {
        HashSet<String> tokenSet = new HashSet<>();
        if (tokens.contains(method.getName()))
            tokenSet.add(method.getName());
        for (String token: SetTool.union(method.addWords, method.changedWords, method.delWords)) {
            if (tokens.contains(token)) {
                tokenSet.add(token);
            }
        }

        int hash = GroupHash.calculateHash(tokenSet);
        return new Pair<Integer, HashSet<String>>(hash, tokenSet);
    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer();
        List<RevCommit> commits = git.getCommits();
        int commitCount = 0;
        Map<Integer, Integer> resultCount = new HashMap<>();
        resultCount.put(1, 0);
        resultCount.put(3, 0);
        resultCount.put(5, 0);
        resultCount.put(10, 0);
        resultCount.put(11, 0);
        int i = 0;
        for (RevCommit commit: commits) {
            try {
                //Diff diff = new Diff(git, commit.getId());
                Diff diff = new Diff(git, git.getId("1118299c338253cea09640acdc48dc930dc27fda"));
                List<Group> groups = GroupSpliter.split(diff);

                int count = 0;
                for (Group g : groups) count += g.getMethods().size();
                if (count > 20) {
                    System.out.println(commit.getId().toString() + ": " + groups.size() + " in " + count);
                    commitCount ++;
                    int groupSize = groups.size();
                    if (groupSize == 1) resultCount.put(1, resultCount.get(1) + 1);
                    else if (groupSize <= 3) resultCount.put(3, resultCount.get(3) + 1);
                    else if (groupSize <= 5) resultCount.put(5, resultCount.get(5) + 1);
                    else if (groupSize <= 10) resultCount.put(10, resultCount.get(10) + 1);
                    else if (groupSize > 11) resultCount.put(11, resultCount.get(11) + 1);
                }

                i ++;
                if (i % 20 == 0) {
                    System.out.println(i + ":" + commitCount);
                    System.out.println(" 1: " + resultCount.get(1));
                    System.out.println(" 3: " + resultCount.get(3));
                    System.out.println(" 5: " + resultCount.get(5));
                    System.out.println(" 10: " + resultCount.get(10));
                    System.out.println(" 11: " + resultCount.get(11));
                }

            } catch ( Exception e) {
                e.printStackTrace();
                System.out.println(commit.getId());
                System.out.println("   error");
            }
        }
    }
}
