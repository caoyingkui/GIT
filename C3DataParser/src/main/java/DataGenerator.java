import fileDiff.Diff;
import fileDiff.field.ChangedField;
import fileDiff.file.ChangedClass;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import git.GitAnalyzer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by kvirus on 2019/6/22 23:11
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DataGenerator {

    public static class Visitor extends ASTVisitor {
        StringBuilder builder = new StringBuilder();
        public boolean visit(MethodDeclaration node){
            String value = "";
            if (node.modifiers() != null)
                for (Object modifier: node.modifiers())
                    if (modifier instanceof Modifier)
                        builder.append(((Modifier)modifier).toString()).append(" ");
            if (node.getReturnType2() != null)
                value += ":" + node.getReturnType2().toString();
            builder.append(node.getName().toString());
            String parStr = "";
            if (node.parameters().size() > 0) {
                for (Object par:node.parameters()) {

                    //在这里不能正常获取 “double... d1”,只能获取double，而不能获取“double...”
                    if (par instanceof SingleVariableDeclaration) {
                        SingleVariableDeclaration p = (SingleVariableDeclaration) par;
                        String typeName = p.getType().toString();
                        if (par.toString().contains("...")) typeName += "...";
                        parStr += "," + typeName;
                    } else {
                        assert(1 == 2);
                    }
                }
                parStr = parStr.substring(1);
            }
            builder.append("(" + parStr + ")").append(value);
            return false;
        }
    }

    public static Map<String, Commit> parse(String path) {
        Map<String, Commit> commits = new HashMap<String, Commit>();
        BufferedReader reader = null;
        try {
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createJsonParser(new File(path));

            JsonToken current = jp.nextToken();
            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("@class")) {
                current = jp.nextToken(); // 读取数据的值
            } else {
                return null;
            }

            current = jp.nextToken();
            if (!( current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("results") ))
                return null;

            current = jp.nextToken();
            if (!(current == JsonToken.START_ARRAY)) return null;

            Set<String> ids = new HashSet<String>();
            int count = 0;
            while ((current = jp.nextToken()) != JsonToken.END_ARRAY) {
                JsonNode node = jp.readValueAsTree();
                JsonNode members = node.get("cluster").get("members");
                for (int i = 0; i < members.size(); i++) {
                    JsonNode method = members.get(i);
                    String commitId = method.get("commitAfterChange").getTextValue();

                    Method m = new Method();
                    m.id = method.get("id").getTextValue();
                    m.clusterId = count + "";
                    m.fileName = method.get("fileName").getTextValue();
                    m.oldName = method.get("signatureBeforeChange").getTextValue();
                    m.newName = method.get("signatureAfterChange").getTextValue();

                    ids.add(m.id);

                    Commit commit = commits.getOrDefault(commitId, new Commit());
                    commit.addMethod(m);
                    if (!commits.containsKey(commitId)) commits.put(commitId, commit);
                }
                count ++;
            }
            System.out.println(ids.size());
            return commits;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void  generate(String path, GitAnalyzer git, String storePath) {
        System.out.println();
        Map<String, Commit> commits = parse(path);
        int count = 0, total = 0;
        for (String id:commits.keySet()) {
            Commit commit = commits.get(id);

            Iterator<Method> it = commit.methods.iterator();
            while (it.hasNext()) {
                Method m = it.next();
                try {
                    //System.out.println(m.newName);
                    String file = m.fileName;
                    String newName = getName(m.newName);
                    String oldName = getName(m.oldName);
                    m.newContent = getMethodContent(git.getFileFromCommit(git.getCommit(id), file), newName);
                    m.oldContent = getMethodContent(git.getFileFromFormerCommit(git.getCommit(id), file), oldName);
                    if (m.newContent.equals("") || m.oldContent.equals("")) {
                        count ++;
                        it.remove();
                    }
                } catch (Exception e) {
                    m.newContent = m.oldContent = "";
                    it.remove();
                    count ++;
                }
                total ++;
            }
        }

        System.out.println(count + " in " + total);

        count = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(storePath)));
            writer.write("{ \"methods\":[");
            boolean s = false;
            for (String commit: commits.keySet()) {
                for (Method method: commits.get(commit).methods) {
                    writer.write((s ? ",\n" : "") + method.toJson() );
                    count ++;
                    s = true;
                }
                writer.flush();
            }
            writer.write("]}");
            writer.close();
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getName(String string){
        ASTParser parser = ASTParser.newParser(9);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        parser.setSource( (string + "{}").toCharArray());
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);
        TypeDeclaration unit = (TypeDeclaration)parser.createAST(null) ;
        Visitor visitor = new Visitor();
        unit.accept(visitor);
        return visitor.builder.toString();
    }

    public static String getMethodContent(String fileContent, String methodName) {
        ASTParser parser = ASTParser.newParser(9);
        parser.setSource(fileContent.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);
        StringBuilder content = new StringBuilder();
        CompilationUnit unit = (CompilationUnit)parser.createAST(null);
        boolean s = true;
        unit.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getParent().getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) return false;
                Visitor visitor = new Visitor();
                node.accept(visitor);
                if (visitor.builder.toString().equals(methodName)) {
                    if (content.length() > 0) {
                        return false;
                        /*content.replace(0, content.length(), "FALSE");
                        System.out.println("error");
                        return false;*/
                    }
                    if (node.isConstructor()) {
                        content.append("class ").append(node.getName().toString()).append("{\n")
                                .append(node.toString()).append("\n")
                                .append("}");
                    } else {
                        content.append("class temp").append("{\n")
                                .append(node.toString()).append("\n")
                                .append("}");
                    }
                    return false;
                }
                return false;
            }
        });

        return content.toString().equals("FALSE") ? "" : content.toString();
    }

    public static void main(String[] args) {

        for (ProjectInfo.Project project: ProjectInfo.projects) {
            generate(project.originalFile, new GitAnalyzer(project.gitPath), "");
        }
    }
}
