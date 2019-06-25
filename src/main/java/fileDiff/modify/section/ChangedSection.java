package fileDiff.modify.section;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import fileDiff.Change;
import fileDiff.Diff;
import fileDiff.file.ChangedClass;
import fileDiff.file.FileDiff;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.insert.InsertHash;
import fileDiff.group.hash.move.MMove;
import fileDiff.group.hash.update.UUpdate;
import fileDiff.method.ChangedMethod;
import git.GitAnalyzer;
import gumtreediff.utils.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Matrix;
import util.ReaderTool;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;

/**
 * Created by kvirus on 2019/6/3 9:23
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ChangedSection {
    public Change<String> content;

    public ChangedMethod method;

    public boolean isFieldChanged = false;

    public boolean isMethodChanged = false;

    public List<StatementHash> statementHashes = new ArrayList<>();

    @NotNull
    public List<SourceCodeChange> changes;

    public ChangedSection(Change<String> content, ChangedMethod method, List<SourceCodeChange> changes) {
        this.content = content;
        this.changes = changes;
        this.method = method;
        if (content != null) {
            for (SourceCodeChange change : changes) {
                if (change instanceof Update)
                    statementHashes.add(new UUpdate(change));
                else if (change instanceof Move)
                    statementHashes.add(new MMove(change));
                else
                    statementHashes.add(InsertHash.getHash(change));
            }
        }
    }

    public ChangedSection(T t, ChangedMethod method, ChangedClass cfile) {
        this.method = method;
        String oldContent = "", newContent = "";
        if (t.oldStartPos >= 0 && t.oldEndPos >= 0) oldContent = cfile.content.OLD.substring(t.oldStartPos, t.oldEndPos + 1);
        if (t.newStartPos >= 0 && t.newEndPos >= 0) newContent = cfile.content.NEW.substring(t.newStartPos, t.newEndPos + 1);
        content = new Change<String>(newContent, oldContent);
        changes = t.changes;
        if (content != null) {
            for (SourceCodeChange change : changes){
                if (change instanceof Update)
                    statementHashes.add(new UUpdate(change));
                else if (change instanceof Move)
                    statementHashes.add(new MMove(change));
                else
                    statementHashes.add(InsertHash.getHash(change));
            }
        }
    }

    public void getType(Set<String> fields, Set<String> methods) {
        //函数名的改动
        if (content == null) {
            isFieldChanged = true;
            return;
        }

        for (String field: fields) {
            if (content.NEW.contains(field) || content.OLD.contains(field)) {
                isFieldChanged = true;
                break;
            }
        }

        for (String method: methods) {
            if (content.NEW.contains(method) || content.OLD.contains(method)) {
                isMethodChanged = true;
                break;
            }
        }
    }

    public static List<ChangedSection> extractSections(List<SourceCodeChange> changes, ChangedMethod cMethod, ChangedClass cfile) {
        List<T> sections = new ArrayList<>();
        List<SourceCodeChange> signatureChanges = new ArrayList<>();
        for (SourceCodeChange chg: changes) {
            if (isSignatureChange(chg)) {
                signatureChanges.add(chg);
                continue;
            }
            T t = new T(chg, cfile);
            boolean needMoreCheck = true;
            //发现可合并的后，先合并，然后需要继续检查合并之后的结果是否仍然可以和其他进行合并
            while(needMoreCheck) {
                needMoreCheck = false;
                for (T section: sections) {
                    if (section.merge(t, cfile)) {
                        needMoreCheck = true;
                        t = section;
                        sections.remove(section);
                        break;
                    }
                }
            }
            sections.add(t);
        }

        List<ChangedSection> result = new ArrayList<>();
        //把签名的修改单独处理
        if (signatureChanges.size() > 0) {
            cMethod.changedWords.add(cMethod.getName());
            ChangedSection s = new ChangedSection(null, cMethod, signatureChanges);
            result.add(s);
        }

        for (T section: sections) {
            result.add(new ChangedSection(section, cMethod, cfile));
        }
        return result;
    }

    public static boolean isSignatureChange(SourceCodeChange change) {
        ChangeType type = change.getChangeType();
        return type == ChangeType.PARAMETER_TYPE_CHANGE ||
                type == ChangeType.PARAMETER_INSERT ||
                type == ChangeType.PARAMETER_DELETE ||
                type == ChangeType.PARAMETER_RENAMING ||
                type == ChangeType.PARAMETER_ORDERING_CHANGE ||
                type == ChangeType.RETURN_TYPE_CHANGE ||
                type == ChangeType.RETURN_TYPE_DELETE ||
                type == ChangeType.RETURN_TYPE_INSERT ||
                type == ChangeType.METHOD_RENAMING ||
                type == ChangeType.REMOVING_METHOD_OVERRIDABILITY ||
                type == ChangeType.ADDING_METHOD_OVERRIDABILITY ||
                type == ChangeType.INCREASING_ACCESSIBILITY_CHANGE ||
                type == ChangeType.DECREASING_ACCESSIBILITY_CHANGE;

    }

    public boolean isSimilar(ChangedSection section) {
        ChangedSection s1, s2;
        if (this.statementHashes.size() < section.statementHashes.size()) {
            s1 = this;
            s2 = section;
        } else {
            s1 =  section;
            s2 = this;
        }

        for (StatementHash h1: s1.statementHashes) {
            boolean s = false;
            for (StatementHash h2: s2.statementHashes) {
                if (!(h1.equals(h2))) continue;

                s= true;
                break;
            }

            if (!s) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if (content == null) return "";
        else return content.OLD + "\n\n" + content.NEW;
    }

    private static final class T {
        int newStartPos = -1;
        int newEndPos = -1;
        int oldStartPos = -1;
        int oldEndPos = -1;

        int newStartLine = -1;
        int newEndLine = -1;
        int oldStartLine = -1;
        int oldEndLine = -1;

        List<SourceCodeChange> changes = new ArrayList<>();

        public T(SourceCodeChange chg, ChangedClass cfile) {
            changes.add(chg);
            SourceCodeEntity newEntity = null, changedEntity = null;
            if (chg instanceof Update) {
                Update update = (Update) chg;
                newEntity = update.getNewEntity();
                changedEntity = update.getChangedEntity();
            } else if (chg instanceof Insert) {
                newEntity = chg.getChangedEntity();
            } else if (chg instanceof Delete) {
                changedEntity = chg.getChangedEntity();
            } else if (chg instanceof Move) {
                Move move = (Move) chg;
                newEntity = move.getNewEntity();
                changedEntity = move.getChangedEntity();
            }

            if (newEntity != null) {
                newStartPos = newEntity.getStartPosition();
                newStartLine = cfile.parser.NEW.getLine(newStartPos);
                newEndPos = newEntity.getEndPosition();
                newEndLine = cfile.parser.NEW.getLine(newEndPos);
            }
            if (changedEntity != null) {
                oldStartPos = changedEntity.getStartPosition();
                oldStartLine = cfile.parser.OLD.getLine(oldStartPos);
                oldEndPos = changedEntity.getEndPosition();
                oldEndLine = cfile.parser.OLD.getLine(oldEndPos);
            }
        }

        public boolean merge (T t, ChangedClass cfile) {
            int min = Math.min(newStartLine, t.newStartLine);
            int max = Math.max(newEndLine, t.newEndLine);

            if (min >= 0 && max - min + 1 <= newLines() + t.newLines()) {
                if (newEndPos <= t.newStartPos && cfile.content.NEW.substring(newEndPos + 1, t.newStartPos).trim().length() != 0) return false;

                if (t.newEndPos <= newStartPos && cfile.content.NEW.substring(t.newEndPos + 1, newStartPos).trim().length() != 0) return false;
                merge(t);
                return true;
            }

            min = Math.min(oldStartLine, t.oldStartLine);
            max = Math.max(oldEndLine, t.oldEndLine);

            if (min >= 0 && max - min + 1 <= oldLines() + t.oldLines()) {
                try {
                    if (oldEndPos <= t.oldStartPos && cfile.content.OLD.substring(oldEndPos + 1, t.oldStartPos).trim().length() != 0)
                        return false;

                    if (t.oldEndPos <= oldStartPos && cfile.content.OLD.substring(t.oldEndPos + 1, oldStartPos).trim().length() != 0)
                        return false;
                } catch (Exception e) {
                    int a = 2;
                }
                merge(t);
                return true;
            }

            return false;
        }

        private void merge(T t) {
            changes.addAll(t.changes);
            if (newStartPos > t.newStartPos || newStartPos == -1) newStartPos = t.newStartPos;
            if (newEndPos < t.newEndPos || newEndPos == -1) newEndPos = t.newEndPos;
            if (newStartLine > t.newStartLine || newStartLine == -1) newStartLine = t.newStartLine;
            if (newEndLine < t.newEndLine || newEndLine== -1) newEndLine = t.newEndLine;

            if (oldStartPos > t.oldStartPos || oldStartPos == -1) oldStartPos = t.oldStartPos;
            if (oldEndPos < t.oldEndPos || oldEndPos == -1) oldEndPos = t.oldEndPos;
            if (oldStartLine > t.oldStartLine || oldStartLine == -1) oldStartLine = t.oldStartLine;
            if (oldEndLine < t.oldEndLine || oldStartLine == -1) oldEndLine = t.oldEndLine;
        }

        int newLines() {
            return newEndLine - newStartLine + 1;
        }

        int oldLines() {
            return oldEndLine - oldStartLine + 1;
        }
    }

    public static int getType(ChangedMethod method) {
        if (method.changedWords.size() > 0) return 1;

        for (ChangedSection section: method.changedSections) {
            for (SourceCodeChange change: section.changes) {
                ChangeType type = change.getChangeType();
                if (type == ChangeType.CONDITION_EXPRESSION_CHANGE ) return 2;
                else {
                    EntityType entity = change.getChangedEntity().getType();
                    if (entity == JavaEntityType.IF_STATEMENT || entity == JavaEntityType.ELSE_STATEMENT) return 2;
                }
            }
        }

        for (ChangedSection section: method.changedSections) {
            for (SourceCodeChange change: section.changes) {
                EntityType entity = change.getChangedEntity().getType();
                if (entity == JavaEntityType.TRY_STATEMENT || entity == JavaEntityType.CATCH_CLAUSE) return 3;
            }
        }

        if (method.changedSections.get(0).content == null) return 4;

        if (method.sourceCodeChanges.size() == 1) return 5;
        return 6;
    }

    public static void allMethodsType() throws Exception{
        GitAnalyzer git = new GitAnalyzer();
        int[] nums = {
                0, //全部 0
                0, // 函数 field 1
                0, // if 2
                0, // catch 3
                0, // 签名 4
                0, // 只修改了一行 5
                0, // 未识别 6
        };

        String path = "Data/methods/NUM.txt";
        BufferedWriter[] writers = {
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","0")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","1")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","2")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","3")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","4")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","5")))),
                new BufferedWriter(new FileWriter(new File(path.replace("NUM","6")))),
        };


        int i = 0;
        for (RevCommit commit: git.getCommits()) {
            if (++ i % 100 == 0) {
                System.out.println(i + ":");
                for (int index = 0; index < nums.length; index++) System.out.print(nums[index] + " ");
                System.out.println();
            }


            try {
                //Diff diff = new Diff(git, commit.getId());
                String id = commit.getId().getName();
                System.out.println(id);
                Diff diff = new Diff(git, git.getId("1c47a85f23bb62073880b3604a97305dd33b068b"));

                diff.getClasses().stream().filter(file -> file instanceof ChangedClass && !(file.getName().toLowerCase().contains("test"))).forEach(file -> {
                    ChangedClass cClass = (ChangedClass) file;
                    //String output = id + "id";
                    for (ChangedMethod me : cClass.changedMethods) {
                        int type = getType(me);
                        try {
                            String output = id + ":\n\t" + me.fullName.NEW + "\n";
                            nums[0]++;
                            writers[0].write(output);
                            writers[0].flush();

                            nums[type]++;
                            writers[type].write(output);
                            writers[type].flush();

                            if (type == 6) System.out.println("\t" + me.fullName.NEW);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                continue;
            }

        }
        /*List<ChangedMethod> methods = new ArrayList<>();
        diff.getClasses().stream().filter(file -> !file.getName().toLowerCase().contains("test")).forEach(file -> {
            file.getMethods().stream().filter(m -> m instanceof ChangedMethod)
            .forEach(m -> {
                methods.add((ChangedMethod) m);
            });
        });

        methods.sort((m1, m2) -> m1.fullName.NEW.compareTo(m2.fullName.OLD));

        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (int i = 0; i < methods.size(); i++) {
            map.put(i, new HashSet<>());
        }


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("C:\\Users\\oliver\\Desktop\\output\\output.txt" )));
            for (int i = 0; i < methods.size(); i++) {
                ChangedMethod mi = methods.get(i);

                    for (int j = i + 1; j < methods.size(); j++) {
                        if (i == 12 && j == 19) {
                            int a = 2;
                        }
                        ChangedMethod mj = methods.get(j);
                        boolean s = false;
                        outer:
                        for (ChangedSection si : mi.changedSections) {
                            for (ChangedSection sj : mj.changedSections) {
                                if (si.isSimilar(sj)) {
                                    writer.write(Math.min(i, j) + "_" + Math.max(i, j) + ":\n");
                                    writer.write(si.content.OLD + "\n");
                                    writer.write(si.content.NEW + "\n\n\n");
                                    writer.write(sj.content.OLD + "\n");
                                    writer.write(sj.content.NEW + "\n\n\n");
                                    s = true;
                                    break outer;
                                }
                            }
                        }
                        if (s) {
                            map.get(i).add(j);

                            map.get(j).add(i);
                        }
                    }

            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < methods.size(); i++) {
            System.out.println(methods.get(i).fullName.NEW);
            for (int j: map.get(i)) {
                System.out.print("\t" + methods.get(j).fullName.NEW);
                System.out.println(":" +Math.min(i, j) + "_" + Math.max(i, j));
            }
        }

        int a = 2;*/
    }

    public static void multiSectionMethods() {
        int[] numsTotal = {0, 0, 0, 0, 0, 0, 0};
        GitAnalyzer git = new GitAnalyzer();
        int j = 0;
        for (RevCommit commit: git.getCommits()) {
            j ++;
            try {
                Diff diff = new Diff(git, commit.getId());
                //Diff diff = new Diff(git, git.getId("8cde1277ec7151bd6ab62950ac93cbdd6ff04d9f"));
                int count = 0;
                List<ChangedMethod> methods = new ArrayList<>();
                for (FileDiff file : diff.getClasses()) {
                    if (file.getName().toLowerCase().contains("test")) continue;
                    if (file instanceof ChangedClass) {
                        methods.addAll(((ChangedClass) file).changedMethods);
                    }
                }
                if (methods.size() > 0) {
                     System.out.print(commit.getId().getName() + ": ");
                    int[] nums = {0, 0, 0, 0, 0, 0, 0};
                    for (ChangedMethod method : methods) {
                        nums[getType(method)]++;

                    }

                    nums[0] += methods.size();
                    //System.out.println(nums[1] + "/" + nums[0] );
                    for (int i = 0; i < nums.length; i++) {
                        numsTotal[i] += nums[i];
                        System.out.print(nums[i] + " ");
                    }
                    System.out.println();
                }

                if (j % 200 == 0) {
                    for (int i = 0; i < numsTotal.length; i++) {
                        System.out.print(numsTotal[i] + " ");
                    }
                    System.out.println();
                }
            } catch (Exception e) {
                ;
            }
        }
        for (j = 0; j < numsTotal.length; j++) {
            System.out.print(numsTotal[j] + " ");
        }
    }

    public static void group() {
        GitAnalyzer git = new GitAnalyzer();
        Diff diff = new Diff(git, git.getCommit("ef7a0125133597cea9e15ec011922f6d70a81b97"));

        List<ChangedSection> sections = diff.getChangedSections();

        for (int i = 0; i < sections.size(); i ++) {
            ChangedSection s = sections.get(i);
            System.out.println("-----" + i + "-----");
            System.out.println(s.method.fullName.NEW);
            if (s.content == null) continue;
            if (s.content.OLD != null)
                System.out.println(s.content.OLD);
            System.out.println("***************************");
            if (s.content.NEW != null)
                System.out.println(s.content.NEW);
            System.out.println("\n\n\n");

        }
    }

    public static void testGroup() {
        GitAnalyzer git = new GitAnalyzer();
        //Diff diff = new Diff(git, git.getCommit("ef7a0125133597cea9e15ec011922f6d70a81b97"));
        Diff diff = new Diff(git, git.getCommit("ef7a0125133597cea9e15ec011922f6d70a81b97"));
        List<ChangedSection> sections = diff.getChangedSections();

        int len = sections.size();
        Matrix matrix = new Matrix(len, len);
        for (int i = 5; i < len; i++) {
            ChangedSection s1 = sections.get(i);
            for (int j = i + 1; j < len; j++) {
                ChangedSection s2 = sections.get(j);
                if (s1.isSimilar(s2)) {
                    matrix.setValue(i, j, 1);
                    matrix.setValue(j, i, 1);
                }
            }
        }
        matrix.print(0.1);
        int a = 2;
    }

    public static void main(String[] args) throws IOException {
        //multiSectionMethods();
        //group();
        //testGroup();

        GitAnalyzer git = new GitAnalyzer();
        List<RevCommit> commits = git.getCommits();
        int len = commits.size(), i = 0;
        int[] count = new int[1];
        while(i ++ < 335) {
            Diff diff = new Diff(git, commits.get(len - i));
            diff.getClasses().stream().filter(file -> file instanceof ChangedClass).forEach(
                    file -> {
                        try {
                            ChangedClass cMethod = (ChangedClass) file;
                            count[0] += cMethod.changedMethods.size();
                            System.out.println(count[0]);
                        } catch (Exception e) {
                            int a = 2;
                        }
                    }
            );
        }

        System.out.println(count[0]);
        /*
        String s = ReaderTool.read("C:\\Users\\oliver\\Desktop\\eclipseswt.json");
        JSONObject o = new JSONObject(s);
        JSONArray arr = o.getJSONArray("clusters");
        for (int i = 0; i < arr.length(); i++) {
            JSONArray members = arr.getJSONObject(i).getJSONArray("members");
            for (int j = 0; j < members.length(); j ++) {
                JSONArray diff = members.getJSONObject(j).getJSONArray("diff");
                System.out.println(members.getJSONObject(j).getString("commitAfterChange"));
                for (int k = 0; k < diff.length(); k++) {

                    System.out.println(diff.getString(k).toString());
                }
                System.out.println();
            }
            System.out.println("\n\n\n\n");


        }

        int a = 2;*/
    }
}
