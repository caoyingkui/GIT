package controller;

/**
 * Created by oliver on 2018/11/5.
 */
import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jgit.lib.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;


@EnableScheduling
@Controller
public class WebController {
    List<String> messageList = new ArrayList<>();
    int messageStart = 0;
    GitAnalyzer analyzer = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");


    int id = 0;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/log")
    public void codeSearch() throws Exception{

        List<String> commits = analyzer.getLog();
        JSONArray array = new JSONArray();
        commits.forEach(item -> {array.put(item);});
        template.convertAndSend("/message/commit", commits);
    }

    @MessageMapping("/commit")
    public void getTargetCommit(String commit) throws Exception{
        List<String> files = analyzer.getAllFilesModifiedByCommit(commit);
        String commitMessage = analyzer.getCommitMessage(commit);

        JSONObject object = new JSONObject();
        object.put("message", commitMessage);

        JSONArray array = new JSONArray();
        files.forEach(item -> array.put(item));
        object.put("files", array);

        this.template.convertAndSend("/message/files", object.toString());
    }

    @MessageMapping("/file")
    public void fileSearch(String p) throws Exception{
        JSONObject parameter = new JSONObject(p);
        String targetCommit = parameter.getString("target_commit");
        String targetFile = parameter.getString("target_file");

        ObjectId curId= analyzer.getId(targetCommit);
        String curFile = analyzer.getFileFromCommit(curId, targetFile);

        ObjectId formerId = analyzer.getId(targetCommit + "^");
        String formerFileName = analyzer.getFormerName(curId, targetFile);
        String formerFile = "";
        if (formerFileName != null)
            formerFile = analyzer.getFileFromCommit(formerId, formerFileName);

        JSONObject object = new JSONObject();
        object.put("formerFile", formerFile);
        object.put("curFile", curFile);

        this.template.convertAndSend("/message/diff", object.toString());
    }

    @MessageMapping("/specific_file_commit")
    public void searchCommitsForASpecificFile(String filepath){
        List<Pair<ObjectId, Pair<String, String>>> temp = analyzer.getAllCommitModifyAFile(filepath);
        JSONArray array = new JSONArray();
        for(Pair pair: temp){
            array.put(((ObjectId)(pair.getKey())).getName());
        }

        this.template.convertAndSend("/message/commit", array);
    }

    public void returnResultMessage(){
        this.template.convertAndSend("/message/result", "");
    }
}