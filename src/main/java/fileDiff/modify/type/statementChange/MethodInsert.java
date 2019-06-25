package fileDiff.modify.type.statementChange;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Created by kvirus on 2019/5/24 22:15
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class MethodInsert extends Modify {
    public static int count = 0;

    public Set<String> methods = new HashSet<>();

    public static MethodInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        MethodInsert result = new MethodInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangedEntity().getType().isStatement() &&
                    ( change instanceof Insert || change instanceof Delete)) {
                String statement = change.getChangedEntity().getUniqueName();
                ASTParser parser = ASTParser.newParser(AST.JLS8);
                parser.setSource(statement.toCharArray());
                parser.setKind(ASTParser.K_STATEMENTS);
                // In order to parse 1.5 code, some compiler options need to be set to 1.5
                Map options = JavaCore.getOptions();
                JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
                parser.setCompilerOptions(options);
                Block block = (Block) parser.createAST(null);

                final Set<String> methodNames = new HashSet<>();
                for (Object node: block.statements()) {
                    ((ASTNode)node).accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodInvocation node) {
                            String name = node.getName().toString();
                            if (!( cMethod.content.NEW.contains(name) || cMethod.content.OLD.contains(name) ))
                                methodNames.add(node.getName().toString());
                            return true;
                        }
                    });
                }
                result.methods.addAll(methodNames);
            }
        }

        if (result.methods.size() == 0) {
            result = null;
        } else {
            count ++;
        }
        return result;
    }

    @Override
    protected void build() {

    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void extend(String str) {
        super.extend(str);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    public static void test() {
        String[] ids = {
                "26e672527044f4878926d613c72f742625e81aee",
                "534204890adba5fe7fd69e410aa05a4811811572",
                "35fa0b4f55f95ca0c8d8b21c77e78e478fba8e74",
        };

        String[] fileNames = {
                "xstream/src/java/com/thoughtworks/xstream/core/DefaultConverterLookup.java",
                "lucene/core/src/java/org/apache/lucene/index/IndexWriter.java",
                "lucene/core/src/java/org/apache/lucene/index/StandardDirectoryReader.java"
        };

        String[] methodNames = {
                "setupDefaults",
                "IndexWriter",
                "open"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < ids.length; i++) {
            Diff d = new Diff(git, git.getId(ids[i]));
            FileDiff c = null;
            String targetName = fileNames[i];
            targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
            String methodName = methodNames[i];
            for (FileDiff file: d.getClasses()) {
                if (file.getName().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    MethodInsert result = MethodInsert.match(method);
                    int a = 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }
}
