package analyzer.line;

import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassFile {
    GitAnalyzer git;
    String curFileContent = "";
    String curCommitId = "";

    List<CodeLine> lines = new ArrayList<CodeLine>();
    List<CodeLine> curLines = new ArrayList<>();

    public ClassFile(GitAnalyzer git) {
        this.git = git;
    }

    private void initialize(String commitId, String fileContent) {
        this.curFileContent = fileContent;
        int lineNumber = 0;
        for (String line: getLines(fileContent)) {
            CodeLine codeLine = new CodeLine(commitId, lineNumber ++, line);
            lines.add(codeLine);
            curLines.add(codeLine);
        }
    }

    void rollback(String commitId, String oldFileContent, Patch patch){
        curFileContent = oldFileContent;
        curCommitId = commitId;
        String[] lines = getLines(oldFileContent);
        for(FileHeader file: patch.getFiles()){
            EditList list = null;
            try{
                list = file.toEditList();
            }catch (NullPointerException e){
                continue;
            }

            for (int i = list.size() - 1; i > -1; i --) {
                Edit edit = list.get(i);
                Edit.Type type = edit.getType();
                if (type == Edit.Type.INSERT) {
                    for (int end = edit.getEndB() - 1; end >= edit.getBeginB(); end --){
                        curLines.remove(end);
                    }
                }else if(type == Edit.Type.REPLACE){
                    if(edit.getEndB() - edit.getBeginB() == 1 && edit.getEndA() - edit.getBeginB() == 1) {
                        ; // 需要保留
                    }else{
                        for (int end = edit.getEndB() - 1; end >= edit.getBeginB(); end --){
                            curLines.remove(end);
                        }
                    }
                }
            }

            for (int i = 0; i < list.size(); i ++) {
                Edit edit = list.get(i);
                Edit.Type type = edit.getType();
                if(type == Edit.Type.DELETE){
                    for(int start = edit.getBeginA(); start < edit.getEndA(); start ++) {
                        CodeLine codeLine = new CodeLine(commitId, -1, lines[start]);
                        curLines.add(start, codeLine);
                    }
                }else if(type == Edit.Type.REPLACE){
                    if(edit.getEndB() - edit.getBeginB() == 1 && edit.getEndA() - edit.getBeginB() == 1) {
                        Commit commit = new Commit(commitId, lines[edit.getBeginA()]);
                        curLines.get(edit.getBeginA()).history.add(commit);
                        //curLines.get(edit.getBeginA()).codeLine = lines[edit.getBeginA()];
                    }else{
                        for(int start = edit.getBeginA(); start < edit.getEndA(); start ++) {
                            CodeLine codeLine = new CodeLine(commitId, -1, lines[start]);
                            curLines.add(start,codeLine);
                        }
                    }
                }
            }

        }
    }

    public void retrieveHistory(String filePath) {
        List<Pair<ObjectId, Pair<String, String>>> files = git.getAllCommitModifyAFile(filePath);
        String path = "";
        if(files.size() > 0){

            int index = 0;
            ObjectId id = null;
            for ( ; index < files.size(); index ++) {
                 id = (ObjectId) files.get(index).getKey();
                path = files.get(0).getValue().getValue();
                curFileContent = git.getFileFromCommit(id, path);

                //获取第一个版本非刪除的版本
                if( !curFileContent.equals("") ) {
                    initialize(id.getName(), curFileContent);
                    index++;
                    break;
                }
            }
            for (; index < files.size(); index ++) {

                id = (ObjectId) files.get(index).getKey();
                if(id.getName().equals("5f55ae0b73ec546132f7188490065798bba0ffad")){
                    int a = 2 + 3;
                }

                path = files.get(0).getValue().getValue();
                String fileContent = git.getFileFromCommit(id, path);
                Patch patch= git.getPatch(files.get(index - 1).getKey(), id, path);
                rollback(id.getName(), fileContent, patch);
                if(!isValid()){
                    int a = 2 + 3;
                }
            }
        }
    }

    public List<CodeLine> getLines() {
        return lines;
    }

    public String getCurFileContent() {
        String content = "";
        for (CodeLine line: curLines) {
            content += line.codeLine;
        }
        return content;
    }

    public boolean isValid() {
        String content = getCurFileContent();
        boolean result = content.length() == curFileContent.length();
        if (result) {
            for (int i = 0; i < content.length(); i ++) {
                if ( content.charAt(i) != curFileContent.charAt(i) ){
                   result = false;
                   break;
                }
            }
        }
        return result;
    }

    private String[] getLines(String content){
        //split limit参数的意义在于限制最终生成数据的最大长度，
        //当limit > 0时，即regex最多可以匹配n-1次，数组[n-1]将存储剩余的全部字符
        //当limit = 0时，不限制数组的长度，并且将末尾的空字符串舍弃
        //当limit小于0时，不限制数组的长度，并且保留末尾的空字符
        String[] fileFiles = content.split("\n", -1);
        for (int i = 0; i < fileFiles.length - 1; i ++) {
            fileFiles[i] += "\n";
        }
        if(fileFiles[fileFiles.length - 1].equals(""))
            fileFiles = Arrays.copyOf(fileFiles, fileFiles.length - 1);

        return fileFiles;
    }
}
