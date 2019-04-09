package analyzer.commit;

import analyzer.histories.HistoryAnalyzer;
import analyzer.histories.Issue;
import analyzer.histories.variation.MethodMutantType;
import git.ClassParser;
import git.GitAnalyzer;
import git.Method;
import javafx.util.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import util.WriterTool;

import java.util.*;

public class CommitAnalyzer {

    GitAnalyzer git;

    public CommitAnalyzer(GitAnalyzer git) {
        this.git = git;
    }

    public List<ChangedMethod> start(String commitId) {
        List<ChangedMethod> methods = getMethods(commitId);
        analyzeMethodType(methods);
        return methods;
    }

    public Map<String, Pair<String, String>> getAllFiles(String commitId) {
        Map<String, Pair<String, String>> result = new HashMap<>();
        for (String fileName: git.getAllFilesModifiedByCommit(commitId,  ".java")) {
            Pair<String, String> pair = new Pair<String, String>(
                    git.getFileFromCommit(git.getId(commitId + "^"), fileName),
                    git.getFileFromCommit(git.getId(commitId), fileName)
            );
            result.put(fileName, pair);
        }
        return result;
    }

    public List<ChangedMethod> getMethods(String commitId) {
        List<ChangedMethod> result = new ArrayList<>();
        Map<String, Pair<String, String>> files = getAllFiles(commitId);

        for (String file: files.keySet()) {
            String temp = file.substring(0, file.lastIndexOf("/"));
            if (temp.toLowerCase().startsWith("test"))
                continue;
            Pair<String, String> pair = files.get(file);
            String oldContent = pair.getKey(), newContent = pair.getValue();
            ClassParser newParser = new ClassParser().setSourceCode(newContent), oldParser = new ClassParser().setSourceCode(oldContent);

            Map<String, Method> changeMethods = newParser.getChangedMethod(git.getEditList(oldContent, newContent), true);
            for (String methodName: changeMethods.keySet()) {
                Method method = changeMethods.get(methodName);
                ChangedMethod changedMethod = new ChangedMethod();
                changedMethod.filePath = file;
                try {
                    temp = methodName.substring(0, methodName.indexOf("("));
                    changedMethod.className = temp.substring(temp.lastIndexOf(".") + 1, temp.lastIndexOf(":"));
                    changedMethod.methodName = temp.substring(methodName.indexOf(":") + 1);
                }catch (Exception e) {
                    System.out.println(methodName);
                }
                changedMethod.setFileContent(method.methodContent);

                changedMethod.newlyAdded = !oldParser.contains(methodName);
                result.add(changedMethod);
            }
        }
        return result;
    }

    public void analyzeMethodType(List<ChangedMethod> methods) {
        for (ChangedMethod method1: methods) {
            String log = method1.className + ":\n  call:\n";
            String call = "";
            String dep = "";
            for (ChangedMethod method2: methods) {
                if (method2.contains(method1.className) && method2.contains(method1.methodName)) {
                    call += "    " + method2.filePath + " " + method2.methodName + "\n";
                    method1.beCalled = true;
                    //method2与method1不在同一个路径下
                    if (!method2.fileContent.equals(method1.fileContent)) {
                        method1.externalCalled = true;
                    }
                }

                if (method1.contains(method2.methodName) && method2.contains(method2.className)) {
                    method1.dependent ++;
                    dep += "   " + method2.filePath + " " + method2.methodName + "\n";
                }
            }
            method1.type = MethodMutantType.toType(method1.newlyAdded, method1.beCalled, method1.externalCalled, method1.dependent);
            /*System.out.println(method1.filePath + " " + method1.methodName + ":");
            System.out.println("  call:") ;
            System.out.println(call);
            System.out.println("  dependent:");
            System.out.println(dep);*/
        }
    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        CommitAnalyzer commitAnalyzer = new CommitAnalyzer(git);
        Map<String, Re> issueTypes = new HashMap<>();
        Map<String, Integer> issueTypeSet = new HashMap<>();
        int typeCount = 0;
        Set<String> status = new HashSet<>();
        for (int i = 1; i < 13000 + 8600; i ++) {
            String issueId = "";
            if (i <= 13000)
                issueId = "SOLR-" + i;
            else
                issueId = "LUCENE-" + (i - 13000);
            System.out.println(issueId);
            Issue issue = new Issue(issueId);
            if (issue.id.equals("")) continue;
            if (!issue.details.get("status").equals("Resolved") &&
                !issue.details.get("status").equals("Closed"))
                continue;

            if ( !status.contains(issue.details.get("status"))){
                status.add(issue.details.get("status"));
                System.out.println(issue.details.get("status"));
            }

            if ( !issueTypes.containsKey(issue.details.get("type"))) {
                String type = issue.details.get("type");
                Re re = new Re();
                issueTypes.put(type, re);
            }
            Re re = issueTypes.get(issue.details.get("type"));
            List<RevCommit> commits = HistoryAnalyzer.getAllRelatedCommits(issueId);
            Re row = new Re();
            for (RevCommit commit: commits) {
                List<ChangedMethod> changedMethods = commitAnalyzer.start(commit.getName());
                for (ChangedMethod method: changedMethods) {
                    re.add(method.type);
                    row.add(method.type);
                }
            }
            if (issueTypeSet.containsKey(issue.details.get("type"))){
                WriterTool.append("xboost/data.txt", row.toRow(issueTypeSet.get(issue.details.get("type"))) + "\n");
            } else {
                typeCount ++;
                issueTypeSet.put(issue.details.get("type"), typeCount);
                WriterTool.append("xboost/lables.txt", typeCount + " " + issue.details.get("type") + "\n");
                WriterTool.append("xboost/data.txt", row.toRow(typeCount) + "\n");
            }
        }

        for (String issueType: issueTypes.keySet()) {
            System.out.println(issueType + ":");
            Re re = issueTypes.get(issueType);
            System.out.println(re.toString());
        }

        for (String s: status) {
            System.out.println(s);
        }
    }
}
