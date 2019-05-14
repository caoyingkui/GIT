package fileDiff.file;

import description.Description;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.field.DelField;
import fileDiff.field.FieldDiff;
import fileDiff.field.NewField;
import fileDiff.method.DelMethod;
import fileDiff.method.MethodDiff;
import fileDiff.method.NewMethod;
import fileDiff.type.DiffType;
import git.ClassParser;
import git.Field;
import git.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by kvirus on 2019/4/20 23:26
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ChangedInterface extends FileDiff {
    public Change<String> name;

    Change<String> path;

    Change<String> content;

    Change<ClassParser> parser;

    Change<List<String>> interfaces;

    Change<String> parent;

    public HashSet<NewField> newFields = new HashSet<>();
    public HashSet<DelField> delFields = new HashSet<>();

    public HashSet<NewMethod> newMethods = new HashSet<>();
    public HashSet<DelMethod> delMethods = new HashSet<>();

    public ChangedInterface(String commitId, Diff diff, Change<String> content, Change<String> path, Change<ClassParser> parser) {
        this.commitId = commitId;
        this.diff = diff;
        this.content = content;
        this.path = path;

        this.parser = parser;

        name = new Change<>(parser.NEW.name, parser.OLD.name);

        parse();
    }

    @Override
    public String getName() {
        return name.NEW;
    }

    @Override
    public List<FieldDiff> getFields() {
        List<FieldDiff> result = new ArrayList<>();
        result.addAll(newFields);
        result.addAll(delFields);
        return result;
    }

    @Override
    public List<MethodDiff> getMethods() {
        List<MethodDiff> result = new ArrayList<>();
        result.addAll(newMethods);
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

    @Override
    public void methodDiff() {
        List<Method> addMethods = parser.NEW.getMethods();
        List<Method> oldMethods = parser.OLD.getMethods();

        for (Method m: addMethods) {
            if (!oldMethods.contains(m)) {
                NewMethod method = new NewMethod(m, this, this.commitId);
                method.type = DiffType.Method_Add_In_Interface;
                newMethods.add(method);
            }
        }

        for (Method m: oldMethods) {
            if (!addMethods.contains(m)) {
                DelMethod method = new DelMethod(m, this, this.commitId);
                method.type = DiffType.Method_Delete_In_Interface;
                delMethods.add(method);
            }
        }

    }

    @Override
    public void parse() {
        fieldDiff();
        methodDiff();
    }

    @Override
    protected void printFields(){
        System.out.println(path.NEW);
        System.out.println("    New:");
        for (NewField f: newFields) {
            System.out.println("        " + f.name);
        }
        System.out.println("    Deleted:");
        for (DelField f: delFields) {
            System.out.println("        " + f.name);
        }
    }

    @Override
    protected void printMethods() {
        System.out.println("add methods:");
        for (NewMethod method: newMethods)
            System.out.println("\t" + method.fullName);

        System.out.println("delete methods");
        for (DelMethod method: delMethods)
            System.out.println("\t" + method.fullName);
    }

    public void update(Diff diff) {
        for (MethodDiff method: newMethods) {
            method.update(diff);
        }

        for (MethodDiff method: delMethods) {
            method.update(diff);
        }
    }

    @Override
    public String toString() {
        return name.NEW + " : Interface";
    }

    @Override
    public void matchDescription(List<Description> descriptions) {
        String className = name.NEW;
        for (Description des: descriptions)
            if (des.APIs.contains(className))
                descriptions.add(des);
    }
}
