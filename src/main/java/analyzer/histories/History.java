package analyzer.histories;

import git.Method;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个History实体代表了issue中的某一个段描述或者某一个comment
 * 表示可以表示对应一次修改的一段描述
 */
public class History {
    public String methodName = "";
    public String startCommitId = "";
    public int start = 0;
    public int length = 0;
    public List<Event> events = new ArrayList<>();

    public History(Method method, String commitId) {
        this.methodName = method.fullName;
        this.startCommitId = commitId;
        this.start = method.startLine;
        this.length = method.endLine - method.startLine + 1;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public boolean setEvent(String commitId, String issueId, String issueTitle, String description) {
        boolean result = false;
        for (Event event: events) {
            if (commitId.equals(event.commitId)) {
                event.issueId = issueId;
                event.issueTile = issueTitle;
                event.issueDescription = description;
                result = true;
            }
        }


        return result;
    }

    public Event getLast() {
        return events.size() > 0 ? events.get(events.size() - 1) : null;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("method_name", methodName);
        object.put("start_commit_id", startCommitId);
        object.put("start", start);
        object.put("length", length);

        JSONArray evenArray = new JSONArray();
        for (Event event: events) {
            evenArray.put(event.toJSON());
        }
        object.put("events", evenArray);

        return object;
    }

    public String toString() {
        return toJSON().toString();
    }

}
