package analyzer.line;

import java.util.ArrayList;
import java.util.List;

/**
 * A CodeLine object is a code line of source code file.
 */
public class CodeLine {
    //lineNumber is the original line number of this code line;
    public int lineNumber;
    public String codeLine;

    // history stores all the commit which has been modified this line
    public List<Commit> history = new ArrayList<Commit>();

    public CodeLine(String commitId, int lineNumber, String codeLine){
        this.lineNumber = lineNumber;
        this.codeLine = codeLine;
        history.add(new Commit(commitId, codeLine));
    }

    public int getHistorySize() {
        return history.size();
    }

    public String toString() {
        String line = codeLine;
        for (Commit commit: history) {
            line += "  ==> " + commit.commitId + ": " + commit.message ;
        }
        return line;
    }
}
