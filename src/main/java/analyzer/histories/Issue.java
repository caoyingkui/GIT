package analyzer.histories;

import org.json.JSONArray;
import org.json.JSONObject;
import util.ReaderTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issue {
    public String id = "";
    public String title;
    public Map<String, String> details = new HashMap<>();
    public String description;
    public List<String> attachements = new ArrayList<>();
    public Map<String, String> issueLinks = new HashMap<>();
    public List<Comment> comments;

    public Issue(String issueId) {
        String issueContent = ReaderTool.read("issueCrawler/issue/" + issueId + ".json");
        if (issueContent.equals("")) return ;
        JSONObject object = new JSONObject(issueContent);
        this.id = get("id", object);
        this.title = get("title", object);
        this.description = get("description", object);

        JSONObject detailsObject = (JSONObject) object.get("details");
        details.put("type", get("type", detailsObject));
        details.put("status", get("status", detailsObject));
        details.put("priority", get("priority", detailsObject));
        details.put("resolution", get("resolution", detailsObject));
        details.put("affect_versions", get("affect_versions", detailsObject));
        details.put("labels", get("labels", detailsObject));

        //这两个鬼东西没有处理
        //details.put("fix_versions", (String) detailsObject.get("fix_versions"));
        //details.put("components")

        this.comments = new ArrayList<>();
        JSONArray comments = (JSONArray) object.get("comments");

        comments.forEach(comment -> {
            JSONObject com = (JSONObject) comment;
            com.put("issue_id", this.id);
            com.put("issue_title", this.title);
            this.comments.add( new Comment((JSONObject) comment));
        });

        Comment comment = new Comment();
        comment.issueId = this.id;
        comment.issueTitle = this.title;
        comment.content = this.description;
        this.comments.add(comment);
    }

    private String get(String field, JSONObject json) {
        String result = "";
        try{
            result = (String) json.get(field);
            return result;
        }catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args){
        Issue issue = new Issue("SOLR-1");

    }


}
