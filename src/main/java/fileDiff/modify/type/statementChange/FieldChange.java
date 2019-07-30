package fileDiff.modify.type.statementChange;

import analyzer.histories.variation.Mutant;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.field.ChangedField;
import fileDiff.field.DelField;
import fileDiff.field.NewField;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;
import gumtree.GumTree;
import org.eclipse.jdt.core.dom.ASTParser;
import util.SetTool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/18 20:45
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.STATEMENT)
public class FieldChange extends Modify {
    public static int count = 0;
    public List<Change<String>> changedFields = new ArrayList<>();

    public static FieldChange match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        ChangedMethod cMethod = (ChangedMethod) method;
        Set<String> oldTokens = new HashSet<>(), newTokens = new HashSet<>();
        for (FileDiff file: cMethod.file.diff.getClasses()) {
            file.getFields().forEach(fieldDiff -> {
                String name = fieldDiff.getName();
                if (fieldDiff instanceof ChangedField) {
                    oldTokens.add(((ChangedField) fieldDiff).name.OLD);
                    newTokens.add(name);
                } else if (fieldDiff instanceof NewField) {
                    newTokens.add(name);
                } else if (fieldDiff instanceof DelField) {
                    oldTokens.add(name);
                }
            });
        }

        FieldChange result = new FieldChange();
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            String oldContent = "", newContent = "";
            if (change instanceof Update) {
                Update update = (Update) change;
                newContent = update.getNewEntity().getUniqueName();
                oldContent = update.getChangedEntity().getUniqueName();
            } else if (change instanceof Delete) {
                oldContent = change.getChangedEntity().getUniqueName();
            } else if (change instanceof Insert) {
                newContent = change.getChangedEntity().getUniqueName();
            } else {
                continue;
            }

            List<Mutant> mutants = GumTree.getDifference(newContent, oldContent, ASTParser.K_STATEMENTS);
            if (mutants.size() == 0) mutants = GumTree.getDifference(newContent, oldContent, ASTParser.K_EXPRESSION);
            boolean sig = mutants.size() == 0 ? true : false;
            for (Mutant mutant: mutants) {
                Set<String> tokenInAfter = SetTool.toSet(mutant.after);
                Set<String> tokenInBefore = SetTool.toSet(mutant.before);
                Change<String> changeToken = new Change<String>("", "");
                for (String token: tokenInAfter) {
                    if (newTokens.contains(token)) {
                        changeToken.NEW = token;
                        break;
                    }
                }
                for (String token: tokenInBefore) {
                    if (oldTokens.contains(token)) {
                        changeToken.OLD = token;
                        break;
                    }
                }

                if (changeToken.NEW.length() > 0 || changeToken.OLD.length() > 0) {
                    sig = true;
                    result.changedFields.add(changeToken);
                }
            }
            if (!sig) return null;
        }

        return result.changedFields.size() > 0 && (++count) > 0 ? result : null;
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

    public static void main(String[] args) {
        test();
    }
    
    public static void test() {
        String[] ids = {
                "35fa0b4f55f95ca0c8d8b21c77e78e478fba8e74",
                "7d8fc543f01a31c8439c369b26c3c8c5924ba4ea",
                "1118299c338253cea09640acdc48dc930dc27fda",
                "2da53c32cbcf82139d1053b5f3709cf639ec7971",
                "1118299c338253cea09640acdc48dc930dc27fda",
                "77a4bfaa90637cd3d9a8a2ef4889e163dab143aa",
                "1118299c338253cea09640acdc48dc930dc27fda"
        };
    
        String[] fileNames = {
                "lucene/core/src/java/org/apache/lucene/index/SegmentReader.java",
                "lucene/core/src/java/org/apache/lucene/search/TopDocs.java",
                "lucene/test-framework/src/java/org/apache/lucene/index/AssertingLeafReader.java",
                "lucene/core/src/java/org/apache/lucene/util/packed/Packed16ThreeBlocks.java",
                "lucene/core/src/java/org/apache/lucene/util/bkd/BKDWriter.java",
                "solr/core/src/java/org/apache/solr/schema/SpatialPointVectorFieldType.java",
                "lucene/test-framework/src/java/org/apache/lucene/index/AssertingLeafReader.java"
        };
    
        String[] methodNames = {
                "SegmentReader",
                "MergeSortQueue",
                "visit",
                "ramBytesUsed",
                "OneDimensionBKDWriter",
                "inform",
                "visit"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < ids.length; i++) {
            System.out.println(ids[i]);
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
                    FieldChange result = FieldChange.match(method);
                    System.out.println(result != null);
                }
            }
        }
    }
}
