package analyzer.histories;

import org.json.JSONObject;

public class Comment {
    public String issueId;
    public String issueTitle;
    public EventDate date;
    public String author;
    public String content;

    public Comment() {
    }

    public Comment(JSONObject json){
        this.issueId = get("issue_id", json);
        this.issueTitle = get("issue_title", json);
        this.date = new EventDate(get("date", json));
        this.author = get("author", json);
        this.content = get("content", json);
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
}
