package fileDiff.modify.type.statementChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;
import javafx.util.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/20 8:07
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class NewClassImport extends Modify {
    public static int count = 0;
    public Set<String> newClasses = new HashSet<>();

    public static NewClassImport match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        NewClassImport result = new NewClassImport();
        ChangedMethod cMethod = (ChangedMethod) method;;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change instanceof Insert) {
                Insert insert = (Insert) change;
                SourceCodeEntity entity = insert.getChangedEntity();
                if (entity.getType() == JavaEntityType.VARIABLE_DECLARATION_STATEMENT) {
                    String statement = entity.getUniqueName();
                    Pair<String, Set<String>> p = getVariableDefinition(statement);
                    if (p != null) result.newClasses.add(p.getKey());
                }
            }

        }
        return result.newClasses.size() > 0 && (++count) > 0 ? result : null;
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

    public static Pair<String, Set<String>> getVariableDefinition(String variableDeclaration) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(variableDeclaration.toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        // In order to parse 1.5 code, some compiler options need to be set to 1.5
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);
        try {
            VariableDeclarationStatement declaration = (VariableDeclarationStatement)((Block) parser.createAST(null)).statements().get(0);

            String baseType = declaration.getType().toString();
            Set<String> variables = new HashSet<>();
            for (Object fragment: declaration.fragments()) {
                if (!( fragment instanceof VariableDeclarationFragment )) continue;
                VariableDeclarationFragment f = (VariableDeclarationFragment) fragment;
                variables.add(f.getName().toString());
            }

            return new Pair<String, Set<String>>(baseType, variables);
        } catch (Exception e) {
            return null;
        }
    }

    public static void test() {
        String[] ids = {
                "fd861761f0a82aefe0ffe18ddd154669bae421c2"
        };

        String[] fileNames = {
                "src/java/org/apache/lucene/search/FieldCacheImpl.java"
        };

        String[] methodNames = {
                "getCacheEntries"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < 1; i++) {
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
                    NewClassImport result = NewClassImport.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {
        test();
    }

}
