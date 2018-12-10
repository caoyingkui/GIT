package analyzer;

import config.Path;
import git.ClassParser;
import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;

import java.util.*;

public class FileAnalyzer {
    GitAnalyzer analyzer;
    private Map<String, Integer> changeTimes = new HashMap<String, Integer>();
    private Map<String, Integer> changeTimes_patch_with_one_file = new HashMap<>();
    private int count0 = 0, count1=0, count3=0, count5=0, count10=0;

    public FileAnalyzer(){
        analyzer= new GitAnalyzer(Path.gitPath);
        //poi_analyzer.start();
    }

    public void start(){
        List<String> files = analyzer.getAllFiles(Constants.HEAD, ".java");
        System.out.println(files.size());
        int count = 0;
        for(String file: files){
            if (count % 100 == 0){
                System.out.println("" + count0 + " " + count1 + " " + count3 + " " + count5 + " " + count10);
            }
            System.out.println(file);
            analyzeSingleFile(file);
            count ++;
        }
    }

    public void analyzeSingleFile(String filePath){
        List<Pair<ObjectId, Pair<String,String>>> commitPairs = analyzer.getAllCommitModifyAFile(filePath);
        for(Pair pair: commitPairs) {
            //System.out.println("  " + commit.toString());
            try {
                ObjectId commit = (ObjectId) (pair.getKey());
                Pair<String, String> filePair = (Pair<String,String>)(pair.getValue());
                String newFile = analyzer.getFileFromCommit(commit, filePair.getValue().toString());
                String oldFile = analyzer.getFileFromCommit(analyzer.getId(commit.getName() + "^"), filePair.getKey().toString());
                ClassParser newParser = new ClassParser(newFile);
                Set<String> methodNames = newParser.getAllMethodNames();

                ClassParser oldParser = new ClassParser(oldFile);
                methodNames.addAll(oldParser.getAllMethodNames());

                for(String methodName: methodNames){
                    if(!changeTimes.containsKey(methodName)){
                        changeTimes.put(methodName,0);
                        changeTimes_patch_with_one_file.put(methodName, 0);
                    }
                }

                Patch patch = analyzer.getPatch(commit, null, filePath);
                int ccc = 0;
                for(FileHeader header : patch.getFiles()){
                    ccc ++;
                }

                Set<String> changeMethodName = new HashSet<>();
                changeMethodName.addAll(newParser.getChangedMethod(patch,true));
                changeMethodName.addAll(oldParser.getChangedMethod(patch,false));
                for(String methodName: newParser.getChangedMethod(patch, true)){
                    changeTimes.put(methodName, changeTimes.get(methodName) + 1);
                    if(ccc == 1){
                        changeTimes_patch_with_one_file.put(methodName, changeTimes_patch_with_one_file.get(methodName) + 1);
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
