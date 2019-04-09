import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item {
    public String title;
    public String issueId;
    public Map<String, String> sentences;
    public List<String> codeSnippets;

    public List<List<String>> targets;

    public void setTitle(String title) {
        this.title = title;
        Pattern pattern = Pattern.compile("\\(([0-9]+)\\)");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            issueId = matcher.group(1);
        }
    }

    public Item() {
        sentences = new HashMap<>();
        codeSnippets = new ArrayList<>();
        targets = new ArrayList<>();
    }
}
