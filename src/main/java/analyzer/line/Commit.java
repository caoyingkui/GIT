package analyzer.line;

public class Commit {
    public String commitId;

    //message其实记录的是codeLine的内容
    public String message;

    public Commit(String commitId, String message) {
        this.commitId = commitId;
        this.message = message;
    }
}
