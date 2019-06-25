package analyzer.histories;

import description.Description;
import org.json.JSONObject;
import util.StemTool;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    public String issueId;
    public String issueTitle;
    public EventDate date;
    public String author;
    public String content;

    //将content分成句子后，形成的子评论
    public List<Description> subDescriptions = new ArrayList<>();

    public Comment() {
    }

    public Comment(String issueId, String issueTitle, EventDate date, String author, String content) {
        this.issueId = issueId;
        this.issueTitle = issueTitle;
        this.date = date;
        this.author = author;
        this.content = content;

        split();
    }

    public Comment(JSONObject json){
        this.issueId = get("issue_id", json);
        this.issueTitle = get("issue_title", json);
        this.date = new EventDate(get("date", json));
        this.author = get("author", json);
        this.content = get("content", json);

        split();
    }

    private String get(String fileName, JSONObject json) {
        String result = "";
        try{
            result = (String)json.get(fileName);
        }catch (Exception e) {
            ;
        }finally {
            return result;
        }
    }

    private void split() {
        //for (String sentence: StemTool.string2sentence(content)) {
        for (String sentence: content.split("\n")) {
            if (sentence.length() < 30) continue;
            Description description = new Description(sentence);
            subDescriptions.add(description);
        }
    }
}
