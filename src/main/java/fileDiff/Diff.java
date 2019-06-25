package fileDiff;

import analyzer.histories.Comment;
import analyzer.histories.Issue;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ast.FileUtils;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import description.Description;
import fileDiff.field.ChangedField;
import fileDiff.field.DelField;
import fileDiff.field.FieldDiff;
import fileDiff.field.NewField;
import fileDiff.file.*;
import fileDiff.method.ChangedMethod;
import fileDiff.method.DelMethod;
import fileDiff.method.MethodDiff;
import fileDiff.method.NewMethod;
import fileDiff.modify.section.ChangedSection;
import fileDiff.rationale.Explainable;
import fileDiff.type.FileType;
import git.ClassParser;
import git.GitAnalyzer;
import git.Method;
import javassist.runtime.Desc;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import sun.security.krb5.internal.crypto.Des;

import java.io.File;
import java.util.*;

/**
 * Created by kvirus on 2019/4/9 21:18
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **     **      **     **
 * |  **            *   *        **  **
 * |  **              *          ***
 * |  **              *          **  **
 * |   *******        *          **     **
 */
public class Diff implements Explainable {
    String commitId = "";

    public Map<String, Set<NewField>> newFields = new HashMap<>();
    public Map<String, Set<ChangedField>> changedFields = new HashMap<>();
    public Map<String, Set<DelField>> delFields = new HashMap<>();

    public Map<String, Set<NewMethod>> newMethods = new HashMap<>();
    public Map<String, Set<ChangedMethod>> changedMethods = new HashMap<>();
    public Map<String, Set<DelMethod>> delMethods = new HashMap<>();

    public Set<NewClass> newClasses = new HashSet<>();
    public Set<ChangedClass> changedClasses = new HashSet<>();
    public Set<DelClass> delClasses = new HashSet<>();

    public Set<NewInterface> newInterfaces = new HashSet<>();
    public Set<ChangedInterface> changedInterfaces = new HashSet<>();
    public Set<DelInterface> delInterfaces = new HashSet<>();

    public Diff(GitAnalyzer git, ObjectId objectId) {
        parse(git, objectId);
        update();
    }

    public List<FileDiff> getClasses() {
        List<FileDiff> result = new ArrayList<>();
        result.addAll(newClasses);
        result.addAll(changedClasses);
        result.addAll(delClasses);
        result.addAll(newInterfaces);
        result.addAll(changedInterfaces);
        result.addAll(delInterfaces);

        result.sort((f1, f2)-> f1.getName().compareTo(f2.getName()));
        return result;
    }

    public List<FieldDiff> getFields(String fieldName, boolean withComment) {
        List<FieldDiff> result = new ArrayList<FieldDiff>();
        for (NewClass file: newClasses)
            result.addAll(file.getFields(fieldName, withComment));

        for (NewInterface file: newInterfaces)
            result.addAll(file.getFields(fieldName, withComment));

        for (ChangedClass file: changedClasses)
            result.addAll(file.getFields(fieldName, withComment));

        for (ChangedInterface file: changedInterfaces)
            result.addAll(file.getFields(fieldName, withComment));

        for (DelClass file: delClasses)
            result.addAll(file.getFields(fieldName, withComment));

        for (DelInterface file: delInterfaces)
            result.addAll(file.getFields(fieldName, withComment));
        return result;
    }

    public List<MethodDiff> getMethods(String methodName, boolean withComment) {
        List<MethodDiff> result = new ArrayList<MethodDiff>();
        for (NewClass file: newClasses)
            result.addAll(file.getMethods(methodName, withComment));

        for (NewInterface file: newInterfaces)
            result.addAll(file.getMethods(methodName, withComment));

        for (ChangedClass file: changedClasses)
            result.addAll(file.getMethods(methodName, withComment));

        for (ChangedInterface file: changedInterfaces)
            result.addAll(file.getMethods(methodName, withComment));

        for (DelClass file: delClasses)
            result.addAll(file.getMethods(methodName, withComment));

        for (DelInterface file: delInterfaces)
            result.addAll(file.getMethods(methodName, withComment));
        return result;
    }

    public FileDiff getFileObject(GitAnalyzer git, ObjectId commit, String fileName) {
        //System.out.println(fileName);
        String curContent = git.getFileFromCommit(commit, fileName);
        String formerContent = git.getFileFromFormerCommit(commit, fileName);
        String oldPath = git.getFormerName(commit, fileName);
        if (!formerContent.equals("") || oldPath != null && !oldPath.equals("/dev/null")) {

            Change<String> content = new Change<>(curContent, formerContent);
            Change<String> path = new Change<>(fileName, oldPath);
            Change<ClassParser> parser = new Change<>(
                    new ClassParser().setSourceCode(curContent),
                    new ClassParser().setSourceCode(formerContent)
            );
            if (parser.NEW.type == FileType.Interface) {
                ChangedInterface file = new ChangedInterface(commitId, this, content, path, parser);
                return file;
            } else {
                ChangedClass file = new ChangedClass(commitId, this, content, path, parser);
                return file;
            }
        } else {
            ClassParser parser = new ClassParser().setSourceCode(curContent);
            if (parser.type == FileType.Interface) {
                NewInterface file = new NewInterface(commitId, this, curContent, fileName, parser);
                return file;
            } else {
                NewClass file = new NewClass(commitId, this, curContent, fileName, parser);
                return file;
            }
        }
    }

    private void extractDiff(FileDiff f) {
        if (f instanceof NewInterface) {
            NewInterface file = (NewInterface) f;
            addNewFields(((NewInterface) f).newFields);
            addNewMethods(file.newMethods);
        } else if (f instanceof NewClass) {
            NewClass clazz = (NewClass) f;
            addNewFields(clazz.newFields);
            addNewMethods(clazz.newMethods);
        } else if (f instanceof ChangedInterface) {
            ChangedInterface file = (ChangedInterface) f;
            addNewFields(file.newFields);
            addNewMethods(file.newMethods);

            addDelFields(file.delFields);
            addDelMethods(file.delMethods);
        } else if (f instanceof ChangedClass) {
            ChangedClass file = (ChangedClass) f;
            addNewFields(file.newFields);
            addNewMethods(file.newMethods);

            addDelFields(file.delFields);
            addDelMethods(file.delMethods);

            addChangedFields(file.changedFields);
            addChangedMethods(file.changedMethods);
        }
    }

    private void addNewFields(Set<NewField> fields) {
        for (NewField field: fields) {
            if (!newFields.containsKey(field.name)) newFields.put(field.name, new HashSet<>());
            newFields.get(field.name).add(field);
        }
    }

    private void addChangedFields(Set<ChangedField> fields) {
        for (ChangedField field: fields) {
            if (!changedFields.containsKey(field.name.NEW)) changedFields.put(field.name.NEW, new HashSet<>());
            changedFields.get(field.name.NEW).add(field);
        }
    }

    private void addDelFields(Set<DelField> fields) {
        for (DelField field: fields) {
            if (!delFields.containsKey(field.name)) delFields.put(field.name, new HashSet<>());
            delFields.get(field.name).add(field);
        }
    }

    private void addNewMethods(Set<NewMethod> methods) {
        for (NewMethod method: methods) {
            if (!newMethods.containsKey(method.name)) newMethods.put(method.name, new HashSet<>());
            newMethods.get(method.name).add(method);
        }
    }

    private void addChangedMethods(Set<ChangedMethod> methods) {
        for (ChangedMethod method: methods) {
            if (!changedMethods.containsKey(method.name.NEW)) changedMethods.put(method.name.NEW, new HashSet<>());
            changedMethods.get(method.name.NEW).add(method);
        }
    }

    private void addDelMethods(Set<DelMethod> methods) {
        for (DelMethod method: methods) {
            if (!delMethods.containsKey(method.name)) delMethods.put(method.name, new HashSet<>());
            delMethods.get(method.name).add(method);
        }
    }

    public void parse(GitAnalyzer git, ObjectId commit) {
        this.commitId = commit.getName();
        List<String> files = git.getAllFilesModifiedByCommit(this.commitId, ".java");
        for (String fileName: files) {

            FileDiff f = getFileObject(git, commit, fileName);
            if (f instanceof NewClass) newClasses.add((NewClass) f);
            else if (f instanceof ChangedClass) changedClasses.add((ChangedClass) f);
            else if (f instanceof DelClass) delClasses.add((DelClass) f);
            else if (f instanceof NewInterface) newInterfaces.add((NewInterface) f);
            else if (f instanceof ChangedInterface) changedInterfaces.add((ChangedInterface) f);
            else if (f instanceof DelInterface) delInterfaces.add((DelInterface) f);
            extractDiff(f);
        }

        update();
    }

    public void update() {
        for (NewClass file: newClasses) {
            file.update(this);
        }

        for (ChangedClass file: changedClasses) {
            file.update(this);
        }

        //暂时不考虑删除文件的更新问题
        //for (DelClass file: delClasses) {
        //     file.update(diff);
        //}
    }

    public void updateChange(Diff diff) {

    }

    private void findInterfaceRelation() {
//        Map<String, HashSet<>> imp = new HashMap<>();
//        imp.put("", new FieldDiff())
//        for (NewClass c: newClasses) {
//            for (String name: c.interfaces) {
//                HashSet set = imp.getOrDefault(name, new HashSet<>());
//                set.add(c);
//                if (!imp.containsKey(name)) imp.put(name, c);
//            }
//        }
//
//
//
//        for (NewInterface i : newInterfaces) {
//            Set<String> signatures = new HashSet<>();
//            for (NewMethod m: i.newMethods)
//                signatures.add(m.signature);
//
//
//            for (NewClass newClass: newClasses) {
//                if (newClass.interfaces.contains())
//            }
//        }
    }

    private void findInNewClass () {
        Map<String, HashSet<NewClass>> imp = new HashMap<>();
        for (NewClass newClass: newClasses) {
            for (String name: newClass.interfaces) {
                HashSet<NewClass> classes = imp.getOrDefault(name, new HashSet<>());
                classes.add(newClass);
                if (!imp.containsKey(name)) imp.put(name, classes);
            }
        }

        newInterfaces.stream().filter(i -> imp.containsKey(i.name))
                .forEach(i -> {
                    Set<String> methods = i.getMethodSignatures();
                    ;
                });
    }

    public List<ChangedSection> getChangedSections() {
        List<ChangedSection> sections = new ArrayList<>();
        getClasses().stream().filter(file -> file instanceof ChangedClass).forEach(file -> {
            ChangedClass cClass = (ChangedClass) file;
            for (ChangedMethod m: cClass.changedMethods) {
                sections.addAll(m.changedSections);
            }
        });
        sections.sort((s1, s2) -> s1.toString().compareTo(s2.toString()));
        return sections;
    }

    @Override
    public void matchDescription(List<Description> descriptions) {
        Set<String> classNames = new HashSet<>();
        Set<String> methodNames = new HashSet<>();
        Set<String> fieldNames = new HashSet<>();
        for (FileDiff file: this.getClasses()) {
            classNames.add(file.getName());
            methodNames.addAll(file.getChangedMethodNames());
            fieldNames.addAll(file.getChangedFiledNames());
        }

        for (Description des: descriptions)
            des.recognize(classNames, methodNames, fieldNames);

        for (FileDiff file: this.getClasses()) {
            List<Description> deses = new ArrayList<>();
            String fileName = file.getName();
            if (fileName.toLowerCase().contains("test")) continue;
            if (fileName.equals("BKDReader")) {
                int a = 2;
            }
            descriptions.forEach(des -> {
                if (des.classes.size() == 0) {
                    //deses.add(des);
                } else {

                    if (des.classes.contains(fileName)) {
                        deses.add(des);
                    }
                }
            });

            file.matchDescription(deses);
        }

    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer();
        for (RevCommit commit: git.getCommits()) {
            //
            String msg = git.getCommitMessage(commit.getName());

            if (msg.contains("134091")) {
                System.out.println(commit.getId().toString());
                System.out.println("\t" + msg.substring(0, Math.min(msg.length(), 100)));
            }
         }
    }
}
