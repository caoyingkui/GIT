package analyzer.histories;

import org.json.JSONObject;

public class Event {
    public String commitId = "";
    public String commitMessage = "";

    public String newContent = "";
    public String oldContent = "";

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

    public String toString() {
        return toJSON().toString();
    }
}
