package analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import config.Path;
import git.ClassParser;
import git.GitAnalyzer;
import graph.Graph;
import graph.HashSolver;
import org.eclipse.jgit.lib.ObjectId;
import util.WriterTool;

import java.io.*;
import java.util.*;

/**
 * CommitDependencyAnalyzer用于解析在某次提交中，被修改的文件之间的相互依赖。
 */
public class CommitDependencyAnalyzer {
    public GitAnalyzer gitAnalyzer;
    private Map<String, Integer> apiChangeTimes = new HashMap<>();

    ObjectId id;

    public CommitDependencyAnalyzer(String codeId){
        gitAnalyzer = new GitAnalyzer(Path.gitPath);
        this.id = gitAnalyzer.getId(codeId);
        pathInitialize();
    }

    private void pathInitialize(){
        try {
            //清空APIs文件内的内容
            BufferedWriter reader = new BufferedWriter(new FileWriter(new File(Path.APIs)));
            reader.close();

            //清空路径下的文件
            File[] fileList = new File(Path.diffDir).listFiles();
            for(File file: fileList){
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeFile(String path, String content){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            writer.write(content);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Graph start(String commitId){
        this.id = gitAnalyzer.getId(commitId);
        List<String> files = gitAnalyzer.getAllFilesModifiedByCommit(id.getName(), ".java");
        List<File> fileList = new ArrayList<>();
        List<String> fileNameList = new ArrayList<>();
        for(String file: files){
            String fileContent = gitAnalyzer.getFileFromCommit(id, file);
            String path = Path.diffDir + "/" + file.substring(file.lastIndexOf("/") + 1);
            writeFile(path, fileContent);
            fileList.add(new File(path));
            fileNameList.add(file);
        }

        Set<String> methods = getAllMethods(this.id, fileNameList, true);
        WriterTool.append(Path.APIs, new ArrayList<>(methods));
        Graph graph = getRelation(fileList);
        return graph;
    }


    /**
     * getAllMethods从一次提交中所涉及的java中，获取所有的修改的方法
     * @param commitId
     * @param files
     * @return
     */
    public Set<String> getAllMethods(ObjectId commitId, List<String> files, boolean changed){
        Set<String> result = new HashSet<>();
        try {
            for (String file : files) {
                ClassParser classParser = new ClassParser(gitAnalyzer.getFileFromCommit(commitId, file));
                Set<String> methods = null;
                if (changed)
                    methods = classParser.getChangedMethod(gitAnalyzer.getPatch(commitId, null, file), true);
                else
                    methods = classParser.getAllMethods();
                result.addAll(methods);
            }
        }catch (Exception e){
            System.out.println(" error + " + commitId.getName() + "\n");
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Graph getRelation(List<File> files){
        Graph graph = new Graph();
        HashSolver solver = new HashSolver();
        solver.loadApis();
        Set<String> methods = solver.getLibraryApis().keySet();
        for(String method: methods){
            graph.addMethod(method);
        }

        for(File file: files) {
            List<String> packages = new ArrayList<>();
            try {
                new VoidVisitorAdapter<Object>() {

                    @Override
                    public void visit(PackageDeclaration n, Object arg){
                        super.visit(n,arg);
                        packages.add(n.getNameAsString());
                    }

                    @Override
                    public void visit(ImportDeclaration n, Object arg) {
                        super.visit(n, arg);
                        packages.add(n.getNameAsString());
                    }

                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                    }

                    public void visit(MethodCallExpr n, Object arg) {
                        String name = n.getNameAsString();
                        Node parent = n.getParentNode().get();
                        String methodName = "";
                        while(parent != null){
                            if(parent instanceof MethodDeclaration){
                                methodName = ((MethodDeclaration)parent).getNameAsString();
                                break;
                            }else if(parent instanceof CompilationUnit){
                                break;
                            }
                            parent = parent.getParentNode().get();
                        }
                        graph.addRelation(
                                solver.locateApiFullName(packages, name),
                                solver.locateApiFullName(packages, methodName)
                        );
                        super.visit(n, arg);
                    }
                }.visit(JavaParser.parse(file), null);
                // System.out.println();
            } catch (ParseProblemException | IOException e) {
                System.out.println("Exception found in parsing " + file.getPath());
                new RuntimeException(e);
            }
        }
        return graph;
    }

    public int howManyJavaFilesAreModified(String commitId){
        int result = 0;
        List<String> files = this.gitAnalyzer.getAllFilesModifiedByCommit(commitId);
        for(String file: files){
            if(file.endsWith(".java")){
                result ++;
            }
        }
        return result;
    }

    public int howManyMethodsAreModified(String commitId){
        int result = 0;
        List<String> files = gitAnalyzer.getAllFilesModifiedByCommit(commitId, ".java");
        Set<String> methods = getAllMethods(gitAnalyzer.getId(commitId), files, false);
        for(String method: methods){
            if(!apiChangeTimes.containsKey(method))
                apiChangeTimes.put(method, 0);
        }
        methods = getAllMethods(gitAnalyzer.getId(commitId), files, true);
        for(String method: methods){
            apiChangeTimes.put(method, apiChangeTimes.get(method) + 1);
        }

        return methods.size();
    }

    public static void main(String[] args){
        /*
        CommitDependencyAnalyzer analyzer = new CommitDependencyAnalyzer("Head");
        List<String> commits = analyzer.gitAnalyzer.getLog();
        int total = 0;
        int method_total = 0;
        int method_pre = 0;
        int one = 0;
        int not_zero = 0;
        for(String commit: commits){
            total ++;
            analyzer.pathInitialize();
            Graph graph = analyzer.start(commit);

            int size = graph.getTop().size();
            method_total += graph.size;
            method_pre += size;
            if(size == 1){
                one ++;
            }

            if(graph.size > 0) not_zero ++;
            System.out.println(commit + ": " + graph.size + " " + size);

            if(total % 200 == 0){
                System.out.println("total: " + total);
            }
        }
        System.out.println(String.format("method_total:%d, method_pre:%d, one:%d, not_zeor:%d",
                method_total, method_pre, one, not_zero));
                */


        CommitDependencyAnalyzer analyzer = new CommitDependencyAnalyzer("Head");
        List<String> commits = analyzer.gitAnalyzer.getLog();
        int total = 0;
        int count = 0;
        for(String commit: commits){
            analyzer.pathInitialize();
            //commit = "f1a30bfb00cc3f72ee30a3356c153aee3a402433";
            int size = analyzer.howManyMethodsAreModified(commit);
            total ++;
            count += (size == 1 ? 1 : 0);
            if(total % 200 == 0 ) {
                System.out.println("total: " + total);
            }else if(size == 1){
                System.out.println(commit + ": 1     total: " + total);
            }
        }
        System.out.println("1 fix result:" + count);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Path.apiChangeTimes)));
            int count0, count1, count2, count3, count4_9, count10;
            count0 = count1 = count2 = count3 = count4_9 = count10 = 0;

            for (String method : analyzer.apiChangeTimes.keySet()) {
                int time = analyzer.apiChangeTimes.get(method);
                writer.write(method + " : " + time + "\n");
                if(time == 0)
                    count0 ++;
                else if(time == 1){
                    count1 ++;
                }else if(time == 2){
                    count2 ++;
                }else if(time == 3){
                    count3 ++;
                }else if(time < 10){
                    count4_9 ++;
                }else{
                    count10 ++;
                }
            }
            System.out.println(
                    String.format("change times: 0:%d, 1:%d, 2:%d, 3:%d, 4-9:%d, >=10:%d",
                            count0, count1, count2, count3, count4_9, count10));
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("result:" + count);

    }




}
