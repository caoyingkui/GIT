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
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ReaderTool;
import util.WriterTool;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HistoryAnalyzer {
    private static GitAnalyzer git;
    private Map<String, History> methodHistories = new HashMap<>();
    private static Map<String, RevCommit> commitMap = new HashMap<>();
    private String codeContent = "";
    private static Map<String, List<RevCommit>> issueCommitMap = new HashMap<>();
    static {
        git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");

        //git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\firefox-browser-architecture");
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
            if (pair.getKey().getName().contains("a944ab17a712f58e8417639763a0c7267fa11e61")) {
                int a = 2;
            }
            if (!constructHistorySkeleton(pair))
                break;
        }
    }

    private boolean constructHistorySkeleton(Pair<ObjectId, Pair<String, String>> pair) {
        ObjectId commitId = pair.getKey();
        String oldPath = pair.getValue().getKey();
        String newPath = pair.getValue().getValue();
        if(!newPath.equals(oldPath) && !oldPath.equals("/dev/null")) {
            return false;
        }

        RevCommit commitObject = git.getCommit(commitId.getName());

        String oldFileContent = oldPath.equals("/dev/null") ? "" : git.getFileFromCommit(git.getId(commitId.getName() + "^"), oldPath);
        ClassParser oldParser = new ClassParser().setSourceCode(oldFileContent);
        List<Method> methods = oldParser.getMethods();
        Map<String, Method> oldMethods = new HashMap<>();
        methods.forEach(method -> oldMethods.put(method.fullName, method));

        if (newPath.equals("lucene/core/src/java/org/apache/lucene/search/SynonymQuery.java")) {
            int a = 2;
        }
        String newFileContent = git.getFileFromCommit(commitId, newPath);
        ClassParser newParser = new ClassParser().setSourceCode(newFileContent);
        methods = newParser.getMethods();
        Map<String, Method> newMethods = new HashMap<>();
        methods.forEach(method -> newMethods.put(method.fullName, method));

        for (String method: methodHistories.keySet()) {
            if (method.contains("SynonymWeight")) {
                int a = 2;
            }

            Event last = methodHistories.get(method).getLast();
            String lastFullName = "";
            if (last == null) lastFullName = methodHistories.get(method).methodName;
            else lastFullName = last.oldFullName;

            if (newMethods.containsKey(lastFullName)) {
                Method newMethod = newMethods.get(lastFullName);
                Method oldMethod = oldMethods.getOrDefault(lastFullName, null);
                // oldMethod为空时，或者是该函数第一次添加，或者是存在重命名现象。
                Event event = new Event();
                event.commitId = commitId.getName();
                event.commitMessage = commitObject.getShortMessage();
                event.newFullName = newMethod.fullName;
                event.newName = newMethod.name;
                event.newContent = newMethod.methodContent;
                if (oldMethod == null) {
                    if (method.contains("SynonymWeight")) {
                        int a = 2;
                    }
                    event.oldContent = "";
                    for (String candidate: oldMethods.keySet()) {
                        Method can = oldMethods.get(candidate);
                        if (newMethods.containsKey(can.fullName)) continue;
                        else {
                            if (isSimilar(last == null ? newMethod.methodContent : last.oldContent, can.methodContent)) {
                                event.oldContent = can.methodContent;
                                event.oldFullName = can.fullName;
                                event.oldName = can.name;
                                break;
                            }
                        }
                    }
                } else {
                    event.oldContent = oldMethod.methodContent;
                    event.oldFullName = oldMethod.fullName;
                    event.oldName = oldMethod.name;
                }
                methodHistories.get(method).addEvent(event);
            }

        }

//        for (String method: newMethods.keySet()) {
//            Method newMethod = newMethods.get(method);
//            Method oldMethod = oldMethods.getOrDefault(method, null);
//
//            Event event = new Event();
//            event.commitId = commitId.getName();
//            event.commitMessage = commitObject.getShortMessage();
//            event.newContent = newMethod.methodContent;
//            event.oldContent = oldMethod == null ? "" : oldMethod.methodContent;
//
//            //说明该函数在出現在最终commit中
//            if(methodHistories.containsKey(method)) {
//                methodHistories.get(method).addEvent(event);
//            }
//        }

        return true;
    }

    public static boolean isSimilar(String content1, String content2) {
        String lon = content1.trim(), sht = content2.trim();
        if (lon.length() == 0 || sht.length() == 0) return false;

        if (lon.length() < sht.length()) {
            String temp = lon;
            lon = sht;
            sht = temp;
        }
        int start = 0, len = 0;

        for (len = sht.length() - 1; len > 0; len --) {
            for (int i = 0; i + len < sht.length(); i++) {
                if (lon.contains(sht.substring(i, i + len)))
                    return len > 30;
            }
        }
        return false;
    }

    /**
     * 获取所有与某个issue相关的commit
     * 主要是通过commit中的信息是否包含issueId来判定是与issue
     * @param issueId issueId
     * @return 相关的commit列表
     */
    public static List<RevCommit> getAllRelatedCommits(String issueId) {
        return issueCommitMap.getOrDefault(issueId, new ArrayList<>());
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
            List<String> issues = GitAnalyzer.findIssueId(msg);
            for (String issueId: issues) {
                if ( !issuedHasBeenVisited.contains(issueId)){
                    extractEventsFromIssue(issueId);
                    issuedHasBeenVisited.add(issueId);
                }
            }
            break;
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

    private JSONArray historyGeneration() {
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

        Map<String, List<Comment>> commit_comment = issue.split(relatedCommits);

        List<Pair<Variation, Comment>> result = new ArrayList<>();
        for (RevCommit commit: relatedCommits) {
            List<Variation> variations = new ArrayList<>();
            List<String> files = git.getAllFilesModifiedByCommit(commit.getName(), ".java");
            for (String file: files) {
                //variations.addAll(getVariations(commitId, file));
                variations.addAll(getVariationsByGumTree(commit.getName(), file));
            }
            result.addAll(Matcher.match(variations, commit_comment.getOrDefault(commit.getName(), new ArrayList<>())));
        }

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

        Map<String, Method> oldMethodMap = oldMethods.stream().collect(Collectors.toMap(item -> item.fullName, item -> item, (a, b) -> b));

        for (String methodName: newMethodMap.keySet()) {
            Method method = newMethodMap.get(methodName);
            String newMethodContent = method.methodContent;
            String oldMethodContent = "";
            if (oldMethodMap.containsKey(methodName)) {
                Method oldMethod = oldMethodMap.get(methodName);
                oldMethodContent = oldMethod.methodContent;
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

    private static void generate_issue_commit_map() {
        List<RevCommit> commits = git.getCommits();
        Map<String, List<String>> issue_commit_map = new HashMap<>();
        for (RevCommit commit: commits) {
            String commitMsg = commit.getShortMessage();
            List<String> relatedIssues = GitAnalyzer.findIssueId(commitMsg);
            for (String issue: relatedIssues) {
                if (issue_commit_map.containsKey(issue)) {
                    if (!issue_commit_map.get(issue).contains(commit.getName())){
                        issue_commit_map.get(issue).add(0, commit.getName());
                    }

                } else {
                    List<String> commitList = new ArrayList<>();
                    commitList.add(commit.getName());
                    issue_commit_map.put(issue, commitList);
                }
            }
        }

        StringBuilder fileContent = new StringBuilder();
        for (String issue: issue_commit_map.keySet()) {
            List<String> commitList = issue_commit_map.get(issue);

            StringBuilder temp = new StringBuilder();
            for (String commit: commitList) {
                if (temp.length() > 0) temp.append("|");

                temp.append(commit);
            }

            fileContent.append(issue).append(":").append(temp).append("\n");
        }

        WriterTool.write("issueCrawler/issue_commit.txt", fileContent.toString());

    }

    public static void main(String[] args) {
        generate_issue_commit_map();


        /*GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        HistoryAnalyzer analyzer = new HistoryAnalyzer();
        analyzer.getHistories("solr/contrib/extraction/src/test/org/apache/solr/handler/extraction/ExtractingRequestHandlerTest.java");*/



        //GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        //GitAnalyzer git = new GitAnalyzer("E:\\Intellij workspace\\GIT");


        /*int total = 0;
        double sim = 0;
        for (String issue: issueCommitMap.keySet()) {
            System.out.println(issue);
            List<RevCommit> commits = issueCommitMap.get(issue);

            if (commits.size() > 1) {
                RevCommit first = commits.get(0);
                List<String> files1 = git.getAllFilesModifiedByCommit(first.getName(), ".java");

                for (int i = 1; i < commits.size() ; i ++) {
                    total ++;
                    List<String> files2 = git.getAllFilesModifiedByCommit(commits.get(i).getName(), ".java");
                    double count = 0;
                    for (String file: files1) {
                        if (files2.contains(file)) count ++;
                    }
                    if ((files2.size() + files1.size() - count) > 0)
                        sim += count / (files2.size() + files1.size() - count);
                    files1 = files2;
                }
            }
        }
        System.out.println(sim / total);
*/

        /*查看当issue为bug时，commit有多个
        当为newfeature时，commit只有一个
        int newFeature = 0, newFeatureTotal = 0;
        int bug = 0, bugTotal = 0;
        for (String issue: issueCommitMap.keySet()) {
            Issue i = new Issue(issue);
            if (i == null) continue;
            try {
                if (i.details.get("type").equals("New Feature")) {
                    newFeatureTotal++;
                    if (issueCommitMap.get(issue).size() == 1) {
                        newFeature++;
                    }
                } else if (i.details.get("type").equals("Bug")) {
                    bugTotal++;
                    if (i.attachements.size() > 1) {
                        bug++;
                    } else {
                        int a = 2;
                    }
                }
            } catch (Exception e) {
                ;
            }
        }
        System.out.println(String.format("new_total:%d new:%d, bug_total:%d, bug:%d",
                newFeatureTotal,newFeature,bugTotal, bug));
                */
        /*
        GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        List<RevCommit> commits = git.getCommits();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String issue = scanner.next();
            for (RevCommit commit: commits) {
                String msg = commit.getShortMessage();
                if (msg.contains(issue)) {
                    System.out.print(commit.getName() + " ");
                }
            }
            System.out.println();
        }
        */

        /*统计修改一个issue的commit大于2的比例
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("issueCrawler/issue_commit.txt")));
            String line = "";
            int count = 0;
            int total = 0;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(":",0);
                String issue = temp[0];
                String[] commit = temp[1].split("\\|", 0);
                if (commit.length > 1)
                    count ++;
                total ++;
            }
            reader.close();

            System.out.println(total + " " + count);
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }


}
