package analyzer.histories;

import com.google.common.primitives.Bytes;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.nio.ch.IOUtil;
import util.ReaderTool;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Issue {
    public String id = "";
    public String title;
    public Map<String, String> details = new HashMap<>();
    public String description;
    public List<String> attachements = new ArrayList<>();
    public Map<String, String> issueLinks = new HashMap<>();
    public List<Comment> comments;

    public Issue(String issueId) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue" + System.getProperty("file.separator") + issueId + ".json");
        //String issueContent = ReaderTool.read("issueCrawler/issue/" + issueId + ".json");

        String issueContent = ReaderTool.read(is);
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
        JSONObject attach = (JSONObject)object.get("attachments");
        for (String key: attach.keySet()) {
            attachements.add((String)attach.get(key));
        }

        this.comments = new ArrayList<>();
        List<Comment> original = new ArrayList<>();
        JSONArray originalList = (JSONArray) object.get("comments");

        originalList.forEach(comment -> {
            JSONObject com = (JSONObject) comment;
            com.put("issue_id", this.id);
            com.put("issue_title", this.title);
            original.add(new Comment((JSONObject) comment));
        });

        original.forEach(comment -> {
            String content = comment.content;
            String[] lines = content.split("\\n", 0);
            for (String line: lines) {
                line = line.trim();
                if (line.length() > 30) {
                    Comment subComment = new Comment(comment.issueId,
                            comment.issueTitle,
                            comment.date,
                            comment.author,
                            line);
                    this.comments.add(subComment);

                }
            }
        });

        Comment comment = new Comment();
        comment.issueId = this.id;
        comment.issueTitle = this.title;
        comment.content = this.description;
        this.comments.add(comment);
    }

    public Map<String, List<Comment>> split(List<RevCommit> commits) {
        Map<String, List<Comment>> result = new HashMap<>();
        int commentIndex = 0;
        for (RevCommit commit: commits) {
            //格林威治时间
            long commitTime = commit.getAuthorIdent().getWhen().getTime();
            List<Comment> commentList = new ArrayList<>();
            while (commentIndex < comments.size()) {
                Comment comment = comments.get(commentIndex);
                //加上30秒钟的误差
                try {
                    if (comment.date.time < commitTime + 1000 * 30) {
                        commentList.add(comment);
                        commentIndex++;
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            if (commentList.size() > 0) {
                result.put(commit.getName(), commentList);
            }
        }
        return result;
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
