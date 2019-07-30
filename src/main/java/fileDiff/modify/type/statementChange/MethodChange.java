package fileDiff.modify.type.statementChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Diff;
import fileDiff.file.ChangedClass;
import fileDiff.file.FileDiff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.DelMethod;
import fileDiff.method.MethodDiff;
import fileDiff.method.NewMethod;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kvirus on 2019/5/19 20:50
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class MethodChange extends Modify {
    public static int count = 0;
    public Set<String> changedMethods = new HashSet<>();

    
    public static MethodChange match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;
    
        MethodChange result = new MethodChange();
        ChangedMethod cMethod = (ChangedMethod) method;
        ChangedClass cClass = (ChangedClass) cMethod.file;
        Set<String> newMethods = new HashSet<>();
        Set<String> oldMethods = new HashSet<>();
        for (String methodName: cClass.diff.changedMethods.keySet()) {
            for (ChangedMethod methodDiff : cClass.diff.changedMethods.get(methodName)) {
                if (!(methodDiff.signature.NEW.equals(methodDiff.signature.OLD))) {
                    newMethods.add(methodDiff.name.NEW);
                    oldMethods.add(methodDiff.name.OLD);
                }
            }
        }
        for (String methodName: cClass.diff.delMethods.keySet()) {
            for (DelMethod methodDiff : cClass.diff.delMethods.get(methodName)) {
                oldMethods.add(methodDiff.name);
            }
        }

        for (String methodName: cClass.diff.newMethods.keySet()) {
            for (NewMethod methodDiff : cClass.diff.newMethods.get(methodName)) {
                newMethods.add(methodDiff.name);
            }
        }
        
        
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            String oldStr = "", newStr = "";
            if (!( change.getChangedEntity().getType().isStatement() )) continue;
            if (change instanceof Update) {
                Update update = (Update) change;
                oldStr = update.getChangedEntity().getUniqueName();
                newStr = update.getNewEntity().getUniqueName();
            } else if (change instanceof Delete) {
                Delete delete = (Delete) change;
                oldStr = delete.getChangedEntity().getUniqueName();
            } else if (change instanceof Insert) {
                Insert insert = (Insert) change;
                newStr = insert.getChangedEntity().getUniqueName();
            }
            boolean oldS = false, newS = false;
            if (oldStr.length() > 0) {
                for (String methodName: oldMethods) {
                    if (oldStr.contains(methodName)) {
                        result.changedMethods.add(methodName);
                        oldS = true;
                        break;
                    }
                }
            }
            if (!oldS && newStr.length() > 0) {
                for (String methodName: newMethods) {
                    if (newStr.contains(methodName)) {
                        result.changedMethods.add(methodName);
                        newS = true;
                        break;
                    }
                }
            }
            if (!( newS || oldS || (change.getChangedEntity().getType() == JavaEntityType.VARIABLE_DECLARATION_STATEMENT))) return null;
        }
        
        return result.changedMethods.size() > 0 && (++count) > 0 ? result : null;
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
                "7ba79cf4b1d6a8a6fdf8ea559cdf7526a0dcc808",
                "7d8fc543f01a31c8439c369b26c3c8c5924ba4ea",
                "7d8fc543f01a31c8439c369b26c3c8c5924ba4ea",
                "1118299c338253cea09640acdc48dc930dc27fda",
                "1118299c338253cea09640acdc48dc930dc27fda",
                "74e3ff509e85982a5529ca99c8e3e9ec2f96770a"
        };

        String[] fileNames = {
                "xstream/src/java/com/thoughtworks/xstream/converters/basic/CharConverter.java",
                "solr/core/src/java/org/apache/solr/search/SolrIndexSearcher.java",
                "lucene/facet/src/java/org/apache/lucene/facet/FacetsCollector.java",
                "lucene/test-framework/src/java/org/apache/lucene/codecs/asserting/AssertingPointsFormat.java",
                "lucene/core/src/java/org/apache/lucene/index/FieldInfo.java",
                "lucene/sandbox/src/java/org/apache/lucene/geo/Tessellator.java"
        };

        String[] methodNames = {
                "unmarshal",
                "buildTopDocsCollector",
                "doSearch",
                "writeField",
                "update",
                "mortonIsEar"
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
                    MethodChange result = MethodChange.match(method);
                    System.out.println(result != null);
                }
            }
        }
    }
}
