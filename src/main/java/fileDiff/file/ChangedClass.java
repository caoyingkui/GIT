package fileDiff.file;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import description.Description;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.method.ChangedMethod;
import fileDiff.method.DelMethod;
import fileDiff.method.MethodDiff;
import fileDiff.field.ChangedField;
import fileDiff.field.DelField;
import fileDiff.field.FieldDiff;
import fileDiff.field.NewField;
import fileDiff.method.NewMethod;
import fileDiff.type.DiffType;
import git.ClassParser;
import git.Field;
import git.Method;
import javafx.util.Pair;
import javassist.runtime.Desc;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

import static ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType.*;

/**
 * Created by kvirus on 2019/4/20 19:29
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ChangedClass extends FileDiff implements Update {
    public Change<String> name;

    public Change<String> path;

    public Change<String> content;

    public Change<ClassParser> parser;

    public Change<List<String>> interfaces;

    public Change<String> parent;

    public HashSet<NewField> newFields = new HashSet<>();
    public HashSet<ChangedField> changedFields = new HashSet<>();
    public HashSet<DelField> delFields = new HashSet<>();

    public HashSet<NewMethod> newMethods = new HashSet<>();
    public HashSet<ChangedMethod> changedMethods = new HashSet<>();
    public HashSet<DelMethod> delMethods = new HashSet<>();

    public FileDistiller distiller;
    public List<SourceCodeChange> sourceCodeChanges;

    public ChangedClass(String commitId, Diff diff, Change<String> content, Change<String> path, Change<ClassParser> parser) {
        this.commitId = commitId;
        this.diff = diff;
        this.content = content;
        this.path = path;

        this.parser = parser;

        name = new Change<>(parser.NEW.name, parser.OLD.name);

        parent = new Change<>(parser.NEW.parent, parser.OLD.parent);

        interfaces = new Change<>(parser.NEW.interfaces, parser.OLD.interfaces);

        parse();
    }

    @Override
    public HashSet<String> getChangedFiledNames() {
        if (changedFiledNames == null) {
            changedFiledNames = new HashSet<>();
            for (ChangedField field :changedFields) {
                changedFiledNames.add(field.name.OLD);
                changedFiledNames.add(field.name.NEW);
            }

            for (NewField field : newFields)
                changedFiledNames.add(field.name);

            for (DelField field : delFields)
                changedFiledNames.add(field.name);
        }
        return changedFiledNames;
    }

    @Override
    public HashSet<String> getChangedMethodNames() {
        if (changedMethodNames == null) {
            changedMethodNames = new HashSet<>();
            for (ChangedMethod method: changedMethods) {
                changedMethodNames.add(method.name.OLD);
                changedMethodNames.add(method.name.NEW);
            }

            for (NewMethod method: newMethods)
                changedMethodNames.add(method.name);

            for (DelMethod method: delMethods)
                changedMethodNames.add(method.name);
        }

        return changedMethodNames;
    }

    @Override
    public String getName() {
        return name.NEW;
    }

    @Override
    public String getPath() {
        return path.NEW;
    }

    @Override
    public List<FieldDiff> getFields() {
        List<FieldDiff> result = new ArrayList<>();
        result.addAll(newFields);
        result.addAll(changedFields);
        result.addAll(delFields);
        return result;
    }

    @Override
    public List<MethodDiff> getMethods() {
        List<MethodDiff> result = new ArrayList<>();
        result.addAll(newMethods);
        result.addAll(changedMethods);
        result.addAll(delMethods);
        return result;
    }

    @Override
    public List<FieldDiff> getFields(String fieldName, boolean withComment) {
        List<FieldDiff> result = new ArrayList<FieldDiff>();
        for (NewField field: newFields) {
            if (field.name.equals(fieldName) && (!withComment || field.comment.length() > 0))
                result.add(field);
        }

        for (DelField field: delFields) {
            if (field.name.equals(fieldName) && (!withComment || field.comment.length() > 0))
                result.add(field);
        }

        for (ChangedField field: changedFields) {
            if( (field.name.NEW.equals(fieldName) || field.name.OLD.equals(fieldName)) &&
                    (!withComment || field.comment.NEW.length() > 0 || field.comment.OLD.length() > 0))
                result.add(field);
        }
        return result;
    }

    @Override
    public List<MethodDiff> getMethods(String methodName, boolean withComment) {
        List<MethodDiff> result = new ArrayList<MethodDiff>();
        for (NewMethod method: newMethods) {
            if (method.name.equals(methodName) && (!withComment || method.comment.length() > 0)) {
                result.add(method);
            }
        }

        for (DelMethod method: delMethods) {
            if (method.name.equals(methodName) && (!withComment || method.comment.length() > 0)) {
                result.add(method);
            }
        }

        for (ChangedMethod method: changedMethods) {
            if ( (method.name.NEW.equals(methodName) || method.name.OLD.equals(methodName))
                    && (!withComment || method.comment.NEW.length() > 0 || method.comment.OLD.length() > 0)) {
                result.add(method);
            }
        }
        return result;
    }

    @Override
    public void fieldDiff() {
        List<Field> newSet = parser.NEW.getFields();
        List<Field> oldSet = parser.OLD.getFields();

        oldSet.forEach(old -> {
            if (!newSet.contains(old)) {
                DelField field = new DelField(old.name, old.fullName, old.comment);
                delFields.add(field);
            }
        });

        newSet.forEach(n -> {
            if (!oldSet.contains(n)) {
                NewField field = new NewField(n.name, n.fullName, n.comment);
                newFields.add(field);
            }
        });
    }

    public void methodDiff() {

        distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);
        distiller.extractClassifiedSourceCodeChanges(content.OLD, content.NEW, "");
        distiller.extractClassifiedSourceCodeChanges("", "");
        sourceCodeChanges = distiller.getSourceCodeChanges();

        Map<String, Method> methodsInNew = new HashMap<>();
        for (Method method: parser.NEW.getMethods()) methodsInNew.put(method.fullName, method);
        List<Method> methodsInOld = parser.OLD.getMethods();


        class Relation {
            public Method newMethod = null;
            public Method oldMethod = null;
            public List<SourceCodeChange> changes = new ArrayList<>();
            public boolean newEqual(Method m) { return newMethod != null && newMethod.equals(m);}
            public boolean oldEqual(Method m) { return oldMethod != null && oldMethod.equals(m);}
        }

        List<Relation> relations = new ArrayList<>();
        for (int i = 0; i < sourceCodeChanges.size(); i++) {
            SourceCodeChange change = sourceCodeChanges.get(i);
            Method newMethod = null, oldMethod = null;
            if (change.getRootEntity().getType().isMethod()) {
                try {
                    String uniqueName = change.getRootEntity().getUniqueName().replaceAll(" ", "");
                    newMethod = methodsInNew.getOrDefault(uniqueName, null);
                    if (change instanceof Insert) {
                        if (change.getChangeType() == PARAMETER_INSERT) {
                            oldMethod = methodsInOld.get(parser.OLD.getMethodAt(change.getParentEntity().getStartPosition() + 2));
                        } else {
                            for (Method method : methodsInOld) {
                                if (method.fullName.equals(uniqueName)) {
                                    oldMethod = method;
                                    break;
                                }
                            }
                        }
                    } else if (change instanceof ch.uzh.ifi.seal.changedistiller.model.entities.Update) {
                        oldMethod = methodsInOld.get(parser.OLD.getMethodAt(change.getChangedEntity().getStartPosition()));
                    } else if (change instanceof Move) {
                        oldMethod = methodsInOld.get(parser.OLD.getMethodAt(change.getChangedEntity().getStartPosition()));
                    } else if (change instanceof Delete) {
                        oldMethod = methodsInOld.get(parser.OLD.getMethodAt(change.getChangedEntity().getStartPosition()));
                    }
                } catch (Exception e) {
                    System.out.println("Error from ChangedClass");
                    continue;
                }
                if (newMethod == null && oldMethod == null)
                    continue;
            } else if (change.getChangeType() == ADDITIONAL_FUNCTIONALITY) {
                newMethod = methodsInNew.get(change.getChangedEntity().getUniqueName().replaceAll(" ", ""));
            } else if (change.getChangeType() == REMOVED_FUNCTIONALITY) {
                oldMethod = methodsInOld.get(parser.OLD.getMethodAt(change.getChangedEntity().getStartPosition()));
            } else {
                continue;
            }

            if (newMethod == null && oldMethod == null) {
                String uniqueName = change.getChangedEntity().getUniqueName();

                //????????????????????????????????????
                if (Character.isUpperCase(uniqueName.charAt(uniqueName.lastIndexOf(".") + 1)))
                    continue;
            }

            boolean s = false;
            for (Relation r: relations) {
                if (s) break;
                if (r.newEqual(newMethod) || r.oldEqual(oldMethod)) {
                    if (r.newMethod == null) r.newMethod = newMethod;
                    if (r.oldMethod == null) r.oldMethod = oldMethod;
                    r.changes.add(change);
                    s = true;
                }
            }
            if (!s) {
                Relation r = new Relation();
                r.newMethod = newMethod;
                r.oldMethod = oldMethod;
                r.changes = new ArrayList<>();
                r.changes.add(change);
                relations.add(r);
                if (r.oldMethod == null && r.newMethod == null) {
                    int a = 2;
                }
            }

        }
        for (Relation r: relations) {

            if (r.oldMethod == null) {

                NewMethod method = new NewMethod(r.newMethod, this, commitId);
                newMethods.add(method);
            } else if (r.newMethod == null) {
                DelMethod method = new DelMethod(r.oldMethod, this, commitId);
                delMethods.add(method);
            } else {
                ChangedMethod method = new ChangedMethod(r.newMethod, r.oldMethod, this, commitId);
                method.setSourceCodeChanges(r.changes);
                changedMethods.add(method);
            }
        }
    }

    public void methodDiff1() {
        List<Method> newMethods = parser.NEW.getMethods();
        List<Method> oldMethods = parser.OLD.getMethods();

        List<Method> oldDelMethods = new ArrayList<>(); // ?????????oldMethods???????????????????????????newMethods
        List<Method> methodsFromNew = new ArrayList<>(); // ???????????????newMethods???oldMethods??????????????????newMethods
        List<Method> methodsFromOld = new ArrayList<>(); // ???????????????newMethods???oldMethods??????????????????oldMethods
        List<Method> newAddMethods = new ArrayList<>(); // ?????????newMethods???????????????????????????oldMethods

        //??????????????????????????????newAddMethods???
        //???????????????????????????????????????methodsFromNew???methodsFromOld???
        for (Method newMethod: newMethods) {
            int index = -1;
            Method oldMethod = (index = oldMethods.indexOf(newMethod)) == -1 ? null : oldMethods.get(index);
            if (oldMethod != null) {
                methodsFromNew.add(newMethod);
                methodsFromOld.add(oldMethod);
            } else {
                newAddMethods.add(newMethod);
            }
        }

        //?????????????????????????????????oldDelMethods???
        for (Method oldMethod: oldMethods) {
            int index = -1;
            Method newMethod = (index = newMethods.indexOf(oldMethod) ) == -1 ? null : newMethods.get(index);
            if (newMethod == null) {
                oldDelMethods.add(oldMethod);
            }
        }

        // region <????????????????????????>
        int size = methodsFromNew.size();
        for (int i = 0; i < size; i++) {
            //????????????????????????????????????????????????????????????????????????????????????
            Method newMethod = methodsFromNew.get(i);
            Method oldMethod = methodsFromOld.get(i);

            if(!Method.isIdentical(newMethod, oldMethod)) {
                String methodName = newMethod.name;
                ChangedMethod method = new ChangedMethod(newMethod, oldMethod, this, this.commitId);
                method.type = DiffType.Method_Changed;
                changedMethods.add(method);
            }
        }
        // endregion <????????????????????????>

        // region <?????????????????????????????????>
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Set<Integer> visit = new HashSet<>();
        for (Method newMethod: newAddMethods) {
            String methodName = newMethod.name;
            int index = Method.findSimilarCandidate(newMethod, oldDelMethods);
            Method oldMethod = null;
            if (index != -1) {
                oldMethod = oldDelMethods.get(index);
                visit.add(index);
                ChangedMethod method = new ChangedMethod(newMethod, oldMethod, this, this.commitId);
                method.type = DiffType.Method_Changed;
                this.changedMethods.add(method);
            } else { // ????????????
                NewMethod method = new NewMethod(newMethod, this, this.commitId);
                method.type = DiffType.Method_Add;
                this.newMethods.add(method);
            }
        }

        //???????????????
        size = oldDelMethods.size();
        for (int i = 0; i < size; i++) {
            if (!visit.contains(i)) {
                Method oldMethod = oldDelMethods.get(i);
                DelMethod method = new DelMethod(oldMethod, this, this.commitId);
                method.type = DiffType.Method_Delete;
                delMethods.add(method);
            }
        }
        // endregion <????????????????????????>
    }

    @Override
    public void parse() {
        fieldDiff();
        methodDiff();
        parseFieldRelation();
    }

    @Override
    protected void printFields(){
        System.out.println(path.NEW);
        System.out.println("    New:");
        for (NewField f: newFields) {
            System.out.println("        " + f.name);
        }
        System.out.println("    Changed:");
        for (ChangedField f: changedFields) {
            System.out.println("        " + f.name.OLD + "->" + f.name.NEW);
        }
        System.out.println("    Deleted:");
        for (DelField f: delFields) {
            System.out.println("        " + f.name);
        }
    }

    @Override
    protected void printMethods() {
        System.out.println("changed methods:");
        for (ChangedMethod method: changedMethods)
            System.out.println("\t" + method.fullName.OLD + " -> " + method.fullName.NEW);


        System.out.println("add methods:");
        for (NewMethod method: newMethods)
            System.out.println("\t" + method.fullName);

        System.out.println("delete methods");
        for (DelMethod method: delMethods)
            System.out.println("\t" + method.fullName);
    }

    @Override
    public void update(Diff diff) {
        parseFieldRelation(diff);

        parseInterfaceRelation(diff);

        for (ChangedMethod method: changedMethods) {
            method.update(diff);
        }

        for (NewMethod method: newMethods) {
            method.update(diff);
        }

        for (DelMethod method: delMethods) {
            method.update(diff);
        }


    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * ?????????lucene: 1118299c338253cea09640acdc48dc930dc27fda, lucene/core/src/java/org/apache/lucene/util/bkd/BKDWriter.java????????????
     * ??????????????? int numDims?????????????????????????????? int numDataDims ??? int numDataIndexDims
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void parseFieldRelation() {
        //???????????????????????????
        Set<Change<String>> changeSet = new HashSet<>();
        for (ChangedMethod method: changedMethods) {
            changeSet.addAll(method.getChangedFiled());
        }


        Set<NewField> delNewSet = new HashSet<>();
        Set<DelField> delDelSet = new HashSet<>();
        for (Change<String> change: changeSet) {
            Set<NewField> newSet = new HashSet<>();
            Set<DelField> delSet = new HashSet<>();
            newFields.stream().filter(f -> f.name.equals(change.NEW)).forEach(f -> newSet.add(f));
            delFields.stream().filter(f -> f.name.equals(change.OLD)).forEach(f -> delSet.add(f));

            if (newSet.size() > 0 && delSet.size() > 0) {
                delNewSet.addAll(newSet);
                delDelSet.addAll(delSet);
                for (NewField f1 : newSet) {
                    for (DelField f2 : delSet) {
                        ChangedField field = new ChangedField(
                                new Change<String>(f1.name, f2.name),
                                new Change<String>(f1.fullName, f2.fullName),
                                new Change<String>(f1.comment, f2.comment)
                        );
                        changedFields.add(field);
                    }
                }
            }
        }

        for (NewField field: delNewSet) {
            newFields.remove(field);
        }

        for (DelField field: delDelSet) {
            delFields.remove(field);
        }
    }

    public void parseFieldRelation(Diff diff) {
        for(DelField field: delFields) {
            if (field.comment.length() > 0) continue;
            List<FieldDiff> candidates = diff.getFields(field.name, true);
            for (FieldDiff candidate: candidates) {
                if (!(candidate instanceof DelField)) continue;
                field.comment = ((DelField) candidate).comment;
                break;
            }
        }
        for (NewField field: newFields) {
            if (field.comment.length() > 0) continue;
            List<FieldDiff> candidates = diff.getFields(field.name, true);
            for (FieldDiff candidate: candidates) {
                if (!(candidate instanceof NewField)) continue;
                field.comment = ((NewField) candidate).comment;
                break;
            }
        }

        for (ChangedField field: changedFields) {
            if (field.comment.OLD.length() == 0) {
                List<FieldDiff> candidates = diff.getFields(field.name.OLD, true);
                for (FieldDiff candidate: candidates) {
                    if (!(candidate instanceof ChangedField)) continue;
                    if (!field.name.OLD.equals(((ChangedField) candidate).name.OLD)) continue;
                    field.comment.OLD = ((ChangedField) candidate).comment.OLD;
                    if(field.comment.OLD.length() == 0) continue;
                    break;
                }
            }

            if (field.comment.NEW.length() == 0) {
                List<FieldDiff> candidates = diff.getFields(field.name.NEW, true);
                for (FieldDiff candidate: candidates) {
                    if (!(candidate instanceof ChangedField)) continue;
                    if (!field.name.NEW.equals(((ChangedField) candidate).name.NEW)) continue;
                    field.comment.NEW = ((ChangedField) candidate).comment.NEW;
                    if(field.comment.NEW.length() == 0) continue;
                    break;
                }
            }

        }

        // region <old version>
//        Set<FieldDiff> deldelFields = new HashSet<>();
//        for (DelField sourceField: delFields) {
//            // sourceField ??????????????????????????????
//            // ???????????????????????????????????????????????????
//            // ???????????????????????????????????????????????????????????????????????????????????????????????????
//            List<NewField> targets = new ArrayList<>();
//            diff.newClasses.forEach(file -> {
//                for (NewField field: file.newFields) {
//                    if (field.name.equals(sourceField.name)) {
//                        targets.add(field);
//                    }
//                }
//            });
//
//            if (targets.size() == 1) {
//                NewField targetField = targets.get(0);
//                ChangedField changedField = new ChangedField(
//                        new Change<String>(targetField.name, sourceField.name),
//                        new Change<String>(targetField.fullName, sourceField.fullName),
//                        new Change<String>(targetField.comment, sourceField.comment)
//                );
//                changedFields.add(changedField);
//                deldelFields.add(sourceField);
//            }
//        }
//
//        for (FieldDiff field : deldelFields) {
//            delFields.remove(field);
//        }
        //region <old version>
    }

    @Override
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param diff
     */
    public void parseInterfaceRelation(Diff diff) {
        if (interfaces.NEW.size() == 0) return;

        for (String name: interfaces.NEW) {
            for (NewInterface c: diff.newInterfaces) {
                if (name.equals(c.name))
                    parseInterfaceRelation(c);
            }

            for (ChangedInterface c: diff.changedInterfaces) {
                if (name.equals(c.name.NEW))
                    parseInterfaceRelation(c);
            }
        }
    }

    private void parseInterfaceRelation(FileDiff f) {
        if (f instanceof NewInterface) {
            NewInterface i = (NewInterface) f;
            for (NewMethod mc: newMethods) {
                for (NewMethod mi: i.newMethods) {
                    if (mc.signature.equals(mi.signature))
                        mc.type = DiffType.Method_Add_By_Interface;
                }
            }

            for (ChangedMethod mc: changedMethods) {
                for (NewMethod mi: i.newMethods) {
                    if (mc.signature.equals(mi.signature))
                        mc.type = DiffType.Method_Add_By_Interface;
                }
            }
        } else if (f instanceof ChangedInterface) {
            ChangedInterface i = (ChangedInterface) f;
            for (NewMethod mc: newMethods) {
                for (NewMethod mi: i.newMethods) {
                    if (mc.signature.equals(mi.signature))
                        mc.type = DiffType.Method_Add_By_Interface;
                }
            }

            for (ChangedMethod mc: changedMethods) {
                for (NewMethod mi: i.newMethods) {
                    if (mc.signature.equals(mi.signature))
                        mc.type = DiffType.Method_Add_By_Interface;
                }
            }
        }
    }

    @Override
    public String toString() {
        return name.NEW + " : Class";
    }

}
