package analyzer.histories;

import fileDiff.Diff;
import javafx.util.Pair;
import org.json.JSONObject;

import java.util.*;

public class Event {
    public String commitId = "";
    public String commitMessage = "";

    public String newName = "";
    public String oldName = "";

    public String newFullName = "";
    public String oldFullName = "";

    public String newContent = "";
    public String oldContent = "";

    public List<String> keyWords = new ArrayList<>();

    public String issueId = "";
    public String issueTile = "";
    public String issueDescription = "";

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("commit_id", commitId);
        object.put("commit_message", commitMessage);
        object.put("new_content", newContent);
        object.put("old_content", oldContent);
        object.put("issue_id", issueId);
        object.put("issue_title", issueTile);
        object.put("issue_description", issueDescription);
        return object;
    }



    public void update(Diff diff) {

        Set<String> tokens = new HashSet<>();
        Pair<Set<String>, Set<String>> tokenPair = null; //MethodDiff.extractTokens(newContent, oldContent);
        tokens.addAll(tokenPair.getKey()); // 新增token
        tokens.addAll(tokenPair.getValue()); // 删除token

        updateKeyWords(tokens, diff.changedMethods.keySet());
        //TODO
//        updateKeyWords(tokens, diff.addMethods);
//        updateKeyWords(tokens, diff.deleteMethods);
//        updateKeyWords(tokens, diff.deleteFields);
//        updateKeyWords(tokens, diff.addFields);
    }

    /**
     * tokens一个函数修改部分所包含的keywords, targetTokens是我们认为当前commit中修改部分中存在的关键字
     * 因此呢，我们看tokens是否存在targetTokens中的单词
     * 如果存在，则加入event的keyword中
     * @param tokens
     * @param targetTokens
     */
    private void updateKeyWords(Set<String> tokens, Set<String> targetTokens) {
        if (tokens.size() > targetTokens.size()) {
            for (String token: targetTokens) {
                if (tokens.contains(token))
                    keyWords.add(token);
            }
        } else {
            for (String token: tokens)
                if (targetTokens.contains(token))
                    keyWords.add(token);
        }
    }


    public String toString() {
        return toJSON().toString();
    }
}
