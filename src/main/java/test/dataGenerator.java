package test;

import analyzer.histories.Comment;
import analyzer.histories.Issue;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeModifier;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import description.Description;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.DelMethod;
import fileDiff.method.MethodDiff;
import fileDiff.method.NewMethod;
import fileDiff.modify.section.ChangedSection;
import git.GitAnalyzer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ReaderTool;
import util.StemTool;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kvirus on 2019/5/25 15:51
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class  dataGenerator {

    public static void generate(String issueID, String commitId) {
        File file = new File("test/method-des-data/" + issueID);
        if (file.exists()) return;

        file.mkdir();

        String methodFileName = file.getPath() + "/method.txt";
        GitAnalyzer git = new GitAnalyzer();
        Diff d = new Diff(git, git.getId(commitId));
        int index = 0;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(methodFileName)));
            for (String name : d.changedMethods.keySet()) {
                for (ChangedMethod method : d.changedMethods.get(name)) {
                    writer.write(index + ": " + method.fullName.NEW + "\n");
                    index++;
                }
            }

            for (String name : d.newMethods.keySet()) {
                for (NewMethod method : d.newMethods.get(name)) {
                    writer.write(index + ": " + method.fullName + "\n");
                    index++;
                }
            }

            for (String name : d.delMethods.keySet()) {
                for (DelMethod method : d.delMethods.get(name)) {
                    writer.write(index + ": " + method.fullName + "\n");
                    index++;
                }
            }
            writer.close();

            String desFileName = file.getPath() + "/description.txt";
            writer = new BufferedWriter(new FileWriter(new File(desFileName)));
            Issue issue = new Issue(issueID);
            for (Comment comment: issue.comments) {
                for (Description des: comment.subDescriptions) {
                    writer.write(des.id + ": " + des.description.trim() + "\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            ;
        }

    }

    public static void parallelGenerate() {
        String[] projects = {
                "C:\\Users\\oliver\\Desktop\\Apache projects\\spark",
                "C:\\Users\\oliver\\Desktop\\Apache projects\\poi",
                "C:\\Users\\oliver\\Desktop\\Apache projects\\hadoop",
                "C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr"
        };

        String[] names = {
                "spark",
                "poi",
                "hadoop",
                "lucene"
        };

        for (int i = 0; i < projects.length; i++) {
            String project = projects[i];
            String path = "C:\\Users\\oliver\\Desktop\\Apache projects\\data\\" ;
            final String name = names[i];
            GitAnalyzer git = new GitAnalyzer(project);
            try {
                BufferedWriter oldWriter = new BufferedWriter(new FileWriter(new File(path + "old")));
                BufferedWriter newWriter = new BufferedWriter(new FileWriter(new File(path + "new")));
                JSONArray array = new JSONArray();

//                List<RevCommit> commits = new ArrayList<>();
//                int ii = 0;
//                for (RevCommit c: git.getCommits()) {
//                    commits.add(c);
//                    if (i ++ > 1000) break;
//                }


                git.getCommits().stream().flatMap(commit -> {
                    try {
                        System.out.println(name + ":" + commit.getId().toString());
                        Diff diff = new Diff(git, commit.getId());
                        return diff.getClasses().stream();
                    } catch (Exception e) {
                        return new ArrayList<FileDiff>().stream();
                    }
                }).flatMap(file -> {
                    return file.getMethods().stream();
                }).filter(method -> method instanceof ChangedMethod).forEach(method -> {
                    ChangedMethod cmethod = (ChangedMethod) method;

                    try {
                        for (ChangedSection section : cmethod.changedSections) {
                            if (section.content == null) continue;
                            String oldC = section.content.OLD, oldStr;
                            String newC = section.content.NEW, newStr;

                            JSONObject o = new JSONObject();
                            o.put("old", oldC);
                            o.put("new", newC);
                            array.put(o);


                            if (newC.length() > 0) newC = variableReplace(newC);
                            if (newC.equals("{ }")) {
                                int a = 2;
                            }
                            if (oldC.length() > 0) oldC = variableReplace(oldC);

                            List<String> oldTokens = StemTool.java2Tokens(oldC);
                            if (oldTokens.size() > 0) oldStr = String.join(" ", oldTokens);
                            else oldStr = "<empty>";

                            List<String> newTokens = StemTool.java2Tokens(newC);
                            if (newTokens.size() > 0) newStr = String.join(" ", newTokens);
                            else newStr = "<empty>";

                            if (oldStr.equals("<empty>") && newStr.equals("<empty>")) continue;

                            if ((newStr.replaceAll("\n|\r", " ") + "\n").contains("{ }")) {
                                int a = 2;
                            }
                            oldWriter.write(oldStr.replaceAll("\n|\r", " ") + "\n");
                            newWriter.write(newStr.replaceAll("\n|\r", " ") + "\n");

                        }
                        oldWriter.flush();
                        newWriter.flush();

                    } catch (Exception e) {
                    }
                });
                oldWriter.close();
                newWriter.close();
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "code.json")));
                writer.write(array.toString());
            } catch (Exception e) {
                ;
            }
        }
    }

    public static String variableReplace(String code) {
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(code.toCharArray());

        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);

        Map<String, String> names = new HashMap<>();

        Block block = (Block) parser.createAST(null);
        final ASTVisitor visitor = new ASTVisitor() {
            @Override
            public boolean preVisit2(ASTNode node) {
                if (node instanceof Type) return false;
                if (node instanceof SimpleName) {
                    SimpleName snode = (SimpleName) node;
                    String name = node.toString();
                    char c = name.charAt(0);
                    if (c >= 'a' && c <= 'z' || c == '_') {
                        if (names.containsKey(name)) {
                            snode.setIdentifier(names.get(name));
                        } else {
                            String toName = "V" + (names.size() + 1);
                            names.put(name, toName);
                            snode.setIdentifier(toName);
                        }
                    }
                }

                return true;
            }

            @Override
            public boolean visit(MethodInvocation node) {
                int a = 2;
                if (node.getExpression() != null)
                    node.getExpression().accept(this);
                for (Object p: node.arguments())
                    ((ASTNode)p).accept(this);
                return false;
            }
        };

        block.accept(visitor);

        String result = block.toString();
        return result.substring(result.indexOf("{") + 1, result.lastIndexOf("}") - 1).trim();
    }

    public static void main(String[] args) {
        //toolTokens("");
        //generate("LUCENE-8400", "a2f113c5c65ad2a9cacc45a397f4e33eb403cadb");
        parallelGenerate();
        //System.out.println(variableReplace("int a = b(bb(aa), 1);"));

    }
}































