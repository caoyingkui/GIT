package analyzer.histories;

import analyzer.histories.variation.Mutant;
import analyzer.histories.variation.MutantType;
import analyzer.histories.variation.Variation;
import git.ClassParser;
import git.GitAnalyzer;
import git.Method;
import gumtree.GumTree;
import javafx.util.Pair;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ReaderTool;
import util.WriterTool;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class HistoryAnalyzer {
    private static GitAnalyzer git;
    private Map<String, History> methodHistories = new HashMap<>();
    private static Map<String, RevCommit> commitMap = new HashMap<>();
    private String codeContent = "";
    private static Map<String, List<RevCommit>> issueCommitMap = new HashMap<>();
    static {
        git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        initializeCommit_Map();
        initializeIssue_Commit_Map();

    }


    public HistoryAnalyzer() {
    }

    /**
     * 初始化issue与commits的map
     * 起初时从git中抽取相关信息，然后初始化，但是发现30000个commit，效率着实有点低
     * 现在是把所有的issue与commits的相关映射信息写在文件"issueCrawler/issue_commit.txt"中
     * 每行记录的格式为：issueId:commit{|commit}
     */
    private static void initializeIssue_Commit_Map(){
        String fileContent = ReaderTool.read("issueCrawler/issue_commit.txt");
        String[] lines = fileContent.split("\n", 0);
        for (String line: lines) {
            String[] temp = line.split(":");
            String issueId = temp[0];
            List<RevCommit> commits = new ArrayList<>();
            for (String commitId: temp[1].split("\\|", 0)) {
                commits.add(commitMap.get(commitId));
            }
            issueCommitMap.put(issueId, commits);
        }
    }

    /**
     * 初始所有的commit的id到RevCommit实体的映射
     */
    private static void initializeCommit_Map() {
        List<RevCommit> commits = git.getCommits();
        commits.forEach(commit -> commitMap.put(commit.getName(), commit));
    }

    private void constructHistorySkeleton(String filePath, List<Pair<ObjectId, Pair< String, String >>> commits) {
        methodHistories = new HashMap<>();
        ObjectId firstCommit = commits.get(0).getKey();
        String fileContent = git.getFileFromCommit(firstCommit, filePath);
        codeContent = fileContent;
        ClassParser classParser = new ClassParser().setSourceCode(fileContent);
        List<Method> methods = classParser.getMethods();

        //建立所有在第一个版本中（firstCommit）出现的函数的历史
        methods.forEach(method -> methodHistories.put(method.fullName, new History(method, firstCommit.getName())));

        //回滚所有的提交历史，并从每个commit中抽取每个函数的内容，扩充上步骤建立的每个函数的历史
        for (Pair<ObjectId, Pair<String, String>> pair: commits) {
            if (!constructHistorySkeleton(pair))
                break;
        }
    }

    private boolean constructHistorySkeleton(Pair<ObjectId, Pair<String, String>> pair) {
        ObjectId commitId = pair.getKey();
        String oldPath = pair.getValue().getKey();
        String newPath = pair.getValue().getValue();
        if(!newPath.equals(oldPath)) {
            return false;
        }

        RevCommit commitObject = git.getCommit(commitId.getName());

        String oldFileContent = git.getFileFromCommit(git.getId(commitId.getName() + "^"), oldPath);
        ClassParser oldParser = new ClassParser().setSourceCode(oldFileContent);
        List<Method> methods = oldParser.getMethods();
        Map<String, Method> oldMethods = new HashMap<>();
        methods.forEach(method -> oldMethods.put(method.fullName, method));

        String newFileContent = git.getFileFromCommit(commitId, newPath);
        ClassParser newParser = new ClassParser().setSourceCode(newFileContent);
        methods = newParser.getMethods();
        Map<String, Method> newMethods = new HashMap<>();
        methods.forEach(method -> newMethods.put(method.fullName, method));

        for (String method: newMethods.keySet()) {
            Method newMethod = newMethods.get(method);
            Method oldMethod = oldMethods.getOrDefault(method, null);

            Event event = new Event();
            event.commitId = commitId.getName();
            event.commitMessage = commitObject.getShortMessage();
            event.newContent = newParser.getSubString(newMethod.startLine, newMethod.endLine);
            event.oldContent = oldMethod == null ? "" : oldParser.getSubString(oldMethod.startLine, oldMethod.endLine);

            //说明该函数在出現在最终commit中
            if(methodHistories.containsKey(method)) {
                methodHistories.get(method).addEvent(event);
            }
        }

        return true;
    }

    /**
     * 获取所有与某个issue相关的commit
     * 主要是通过commit中的信息是否包含issueId来判定是与issue
     * @param issueId issueId
     * @return 相关的commit列表
     */
    private List<RevCommit> getAllRelatedCommits(String issueId) {
        return issueCommitMap.get(issueId);
    }

    public String getHistories(String filePath) {
        String historyFilePath = "historyData/data/" + filePath.replace(".java", ".json").replaceAll("/", ".");
        if (new File(historyFilePath).exists()) {
            return ReaderTool.read(historyFilePath);
        }

        List<Pair<ObjectId, Pair< String, String >>> commits = git.getAllCommitModifyAFile(filePath);
        constructHistorySkeleton(filePath, commits);

        Set<String> issuedHasBeenVisited = new HashSet<>();
        for (Pair< ObjectId, Pair<String , String>> pair: commits) {
            String commitId = pair.getKey().getName();
            String msg = commitMap.get(commitId).getShortMessage();
            List<String> issues = findIssueId(msg);
            for (String issueId: issues) {
                if ( !issuedHasBeenVisited.contains(issueId)){
                    extractEventsFromIssue(issueId);
                    issuedHasBeenVisited.add(issueId);
                }
            }
        }
        historyCompact();
        JSONArray array = historyGeneration();

        JSONObject object = new JSONObject();
        object.put("code", codeContent);
        object.put("histories", array);
        String jsonString = object.toString();
        WriterTool.write(historyFilePath, jsonString);
        return jsonString;
    }

    private void historyCompact() {
        for (String methodName: methodHistories.keySet()) {
            History history = methodHistories.get(methodName);
            history.events.removeIf(event -> event.newContent.equals(event.oldContent));
        }
    }

    public JSONArray historyGeneration() {
        JSONArray result = new JSONArray();
        for (String methodName: methodHistories.keySet()) {
            History history = methodHistories.get(methodName);
            result.put(history.toJSON());
        }
        return result;
    }

    /**
     * 根据特定一次issue，查询所有与之相关的commit，并将issue的comment信息匹配到最佳的改动上
     * @param issueId issueId
     */
    private void extractEventsFromIssue(String issueId) {
        Issue issue = new Issue(issueId);
        if (issue.id.equals(""))
            return ;
        List<RevCommit> relatedCommits = getAllRelatedCommits(issueId);


        List<Variation> variations = new ArrayList<>();
        for (RevCommit commit: relatedCommits) {
            List<String> files = git.getAllFilesModifiedByCommit(commit.getName(), ".java");
            for (String file: files) {
                //variations.addAll(getVariations(commitId, file));
                variations.addAll(getVariationsByGumTree(commit.getName(), file));
            }
        }
        List<Pair<Variation, Comment>> result = Matcher.match(variations, issue);
        for (Pair<Variation, Comment> pair: result) {
            Variation variation = pair.getKey();
            Comment comment = pair.getValue();
            if (methodHistories.containsKey(variation.methodName)) {
                History history = methodHistories.get(variation.methodName);
                history.setEvent(variation.commitId, comment.issueId, comment.issueTitle, comment.content);
            }
        }
    }



    double compare(Variation variation, Comment comment) {
        return 0;
    }






    /**
     * getTargetFileName用于给一个模糊的className的时候，给出一个特定的文件名
     * @param className 类名
     * @return 匹配成功的文件完整的地址列表
     */
    public List<String> getTargetFileNames(String className) {
        List<String> result = new ArrayList<>();
        result.add(className);
        return result;
    }

    private List<Variation> getVariationsByGumTree(String commitId, String filePath) {
        List<Variation> result = new ArrayList<>();
        String clazz = "";
        Pattern pattern = Pattern.compile("([a-zA-Z_0-9]+)\\.java");
        java.util.regex.Matcher matcher = pattern.matcher(filePath);
        if (matcher.find()) {
            clazz = matcher.group(1);
        }

        String newFileContent = git.getFileFromCommit(git.getId(commitId), filePath);
        String oldFileContent = git.getFileFromCommit(git.getId(commitId + "^"), filePath);
        ClassParser newClass = new ClassParser().setSourceCode(newFileContent);
        ClassParser oldClass = new ClassParser().setSourceCode(oldFileContent);
        List<Method> newMethods = newClass.getMethods();
        List<Method> oldMethods = oldClass.getMethods();

        Map<String, Method> newMethodMap = new HashMap<>();
        newMethods.forEach(item -> {
            newMethodMap.put(item.fullName, item);
        });

        Map<String, Method> oldMethodMap = new HashMap<>();
        oldMethods.forEach(item -> {
            oldMethodMap.put(item.fullName, item);
        });

        for (String methodName: newMethodMap.keySet()) {
            Method method = newMethodMap.get(methodName);
            String newMethodContent = newClass.getSubString(method.startLine, method.endLine);
            String oldMethodContent = "";
            if (oldMethodMap.containsKey(methodName)) {
                Method oldMethod = oldMethodMap.get(methodName);
                oldMethodContent = oldClass.getSubString(oldMethod.startLine, oldMethod.endLine);
            }


            List<Mutant> difference = GumTree.getDifference(oldMethodContent, newMethodContent);
            Variation variation = new Variation();
            String temp = methodName.substring(0,methodName.lastIndexOf("("));
            temp = temp.substring(temp.lastIndexOf(".") + 1, temp.length());
            variation.addMutant(new Mutant(MutantType.METHODNAME, "", temp));
            variation.addMutant(new Mutant(MutantType.CLASSNAME, "", clazz));
            if (difference != null) {
                variation.commitId = commitId;
                variation.date = new EventDate(commitMap.get(commitId).getCommitTime());
                variation.methodName = method.fullName;
                variation.addMutant(difference);
            }
            result.add(variation);
        }
        return result;
    }

    public Set<Integer> getDeleteLines(FileHeader file) {
        Set<Integer> result = new HashSet<>();
        EditList list = file.toEditList();
        if (list != null) {
            for (Edit edit: list) {
                Edit.Type type = edit.getType();
                int start = edit.getBeginA(), end = edit.getEndA();
                if (type == Edit.Type.DELETE || type == Edit.Type.REPLACE) {
                    for (int line = start; line < end; line ++) {
                        result.add(line);
                    }
                }
            }
        }
        return result;
    }

    public Set<Integer> getInsertLines(FileHeader file) {
        Set<Integer> result = new HashSet<>();
        EditList list = file.toEditList();
        if (list != null) {
            for (Edit edit: list) {
                Edit.Type type = edit.getType();
                int start = edit.getBeginB(), end = edit.getEndB();
                if (type == Edit.Type.INSERT || type == Edit.Type.REPLACE) {
                    for (int line = start; line < end; line ++) {
                        result.add(line);
                    }
                }
            }
        }
        return result;
    }



    /**
     * 从commitMsg中获取issueID
     * @param commitMsg 某次commit中的message
     * @return commitMsg包含的issueId列表
     */
    public static List<String> findIssueId(String commitMsg){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(SOLR-[0-9]+)|(LUCENE-[0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(commitMsg);
        while(matcher.find()){
            result.add(matcher.group(0));
        }
        return result;
    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        HistoryAnalyzer analyzer = new HistoryAnalyzer();
        analyzer.getHistories("solr/contrib/extraction/src/test/org/apache/solr/handler/extraction/ExtractingRequestHandlerTest.java");

    }


}
