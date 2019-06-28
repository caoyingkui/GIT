package git;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import javafx.util.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GitAnalyzer {
    private Git git;
    private Repository repository;

    String patchString = "";
    private ObjectId firstCommit = null;

    public GitAnalyzer(String filePath){
        try {
            git = Git.open(new File(filePath));
            repository = git.getRepository();
            getFirstCommit();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void printfile(File file) {
        if (file.isFile()) {
            System.out.println(file.getAbsolutePath());
        } else if (file.isDirectory()) {
            for (File f: file.listFiles())
                printfile(f);
        }
    }

    public GitAnalyzer(){
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("properties");
            String path = "";
            if (System.getProperty("os.name").equals("Windows 10")) path = bundle.getString("windows_main_git_dir");
            else path = bundle.getString("ubantu_main_git_dir");
            git = Git.open(new File(path));
            //git = Git.open(new File("C:\\Users\\oliver\\Desktop\\firefox-browser-architecture"));
            repository = git.getRepository();
            getFirstCommit();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public RevCommit getCommit(String commitId) {
        try {
            Iterator<RevCommit> commits = git.log().addRange(getId(commitId + "^"), getId(commitId)).call().iterator();
            if (commits.hasNext()) {
                return commits.next();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RevCommit> getCommits(){
        List<RevCommit> result = new ArrayList<>();

        try {
            Iterator<RevCommit> commits = git.log().call().iterator();
            while (commits.hasNext()) {
                RevCommit commit = commits.next();
                result.add(commit);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * getAllCommitModifyAFile函数用于获取所有修改一个文件的commit
     * @param filePath 文件路径
     * @return 一个元组list，其中每一个pair的key是修改文件的commit id，
     *          而value是一个元组，该元组的key为commit前的文件名，value为commit后的文件名。
     */
    public List<Pair<ObjectId, Pair<String, String>>> getAllCommitModifyAFile(String filePath){
        List<Pair<ObjectId, Pair<String, String>>> result = new ArrayList<>();
        try {
            ObjectId endId = repository.resolve("HEAD");
            while(true){
                Iterator<RevCommit> commits = git.log().addPath(filePath)
                        .addRange(firstCommit, endId)
                        .setMaxCount(10000).call().iterator();
                boolean signal = false;
                while(commits.hasNext()){
                    RevCommit commit = commits.next();

                    if(commits.hasNext())
                        result.add(new Pair<ObjectId, Pair<String, String>>(commit, new Pair<String, String>(filePath,filePath)));
                    else{
                        String oldPath = filePath;
                        filePath = getFormerName(commit, filePath);
                        if(filePath != null && !filePath.equals(oldPath)){
                            result.add(new Pair<ObjectId, Pair<String, String>>(commit, new Pair<String, String>(filePath,oldPath)));
                            signal = true;
                            endId = repository.resolve(commit.getName() + "^");
                            break;
                        }else{
                            result.add(new Pair<ObjectId, Pair<String, String>>(commit, new Pair<String, String>(oldPath,oldPath)));
                        }

                    }
                }
                if(!signal)
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getAllFiles(String version, String fileFilter ){
        List<String> result = new ArrayList<>();
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            ObjectId commitId = repository.resolve(version + "^{tree}");
            treeWalk.reset(commitId);

            int count = 0;
            while(treeWalk.next()){
                if(treeWalk.isSubtree()){
                    treeWalk.enterSubtree();
                }else{
                    String path = treeWalk.getPathString();
                    if(fileFilter == null || path.endsWith(fileFilter)){
                        result.add(path);
                        count ++;
                    }
                }
            }
            System.out.println(count);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 从某次提交中获取所有的被修改的文件路径。
     * 当无参数fileFilters时，表示获取所有的文件
     * 否则，fileFilters中的每一个元素代表一种需要的文件类型，用该类型文件名后缀表示，如“.java”
     * @param commitId 特定的commit的id
     * @param fileFilters 需要的文件类型后缀名
     * @return
     */
    public List<String> getAllFilesModifiedByCommit(String commitId, String ...fileFilters){
        List<String > result = new ArrayList<>();
        try {
            RevWalk rw = new RevWalk(repository);
            ObjectId curId = repository.resolve(commitId);
            RevCommit cur = rw.parseCommit(curId);
            RevCommit par = rw.parseCommit(cur.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(par.getTree(), cur.getTree());
            for(DiffEntry diff: diffs){
                String fileName = diff.getNewPath();
                if(fileFilters.length == 0){
                    result.add(fileName);
                }else{
                    for(String filter: fileFilters){
                        if(fileName.endsWith(filter)){
                            result.add(fileName);
                            break;
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println(" error + " + commitId + "\n");
            System.out.println(e.getMessage());
        }
        return result;
    }

    public String getCommitMessage(String commitId){
        String result = "";
        try {
            RevWalk rw = new RevWalk(repository);
            ObjectId curId = repository.resolve(commitId);
            RevCommit cur = rw.parseCommit(curId);
            result = cur.getFullMessage();
        }catch (Exception e){
            System.out.println(" error + " + commitId + "\n");
            System.out.println(e.getMessage());
        }
        return result;
    }


    /**
     * 获取一个文件在特定commit版本时的内容
     * @param commitId commit id
     * @param filePath 文件路径
     * @return 如果commit版本和文件路径合法，则返回文件内容
     *          否则返回空字符串（""）
     */
    public String getFileFromCommit(ObjectId commitId, String filePath){
        String result = "";
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            treeWalk.reset(repository.resolve(commitId.getName()+"^{tree}"));
            treeWalk.setFilter(PathFilter.create(filePath));
            treeWalk.setRecursive(true);
            if(treeWalk.next()){
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                result = new String(loader.getBytes());
            }
        }catch (Exception e){
            System.out.println(" error + " + commitId.getName() + "\n");
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     * 获取一个文件在当前版本，再之前的一个版本的内容
     * 例如commitId 为2， 则返回版本1的文件内容
     * @param commitId 当前commit id
     * @param filePath 文件路径
     * @return 返回内容，如果不存在则返回空字符串（""）
     */
    public String getFileFromFormerCommit(ObjectId commitId, String filePath) {
        //String oldPath = this.getFormerName(commitId, filePath);
        ObjectId formerId = this.getCommit(commitId.getName()+"^");
        //return (oldPath == null || formerId == null )? "" : getFileFromCommit(formerId, oldPath);
        return getFileFromCommit(formerId, filePath);
    }

    private void getFirstCommit(){
        try {
            Iterator<RevCommit> commits = git.log().call().iterator();
            while (commits.hasNext()) {
                firstCommit = commits.next().getId();
            }
            //System.out.println("first commit: " + firstCommit.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getFormerName(ObjectId commitId, String file){
        try {
            ObjectReader objectReader = repository.newObjectReader();
            ObjectLoader objectLoader = objectReader.open(commitId);
            RevCommit commit = RevCommit.parse(objectLoader.getBytes());
            return getFormerName(commit, file);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个file在之前一个版本的路径
     * @param cur
     * @param file
     * @return
     */
    private String getFormerName(RevCommit cur, String file){
        String formerName = null;
        try{
            TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(repository.resolve(cur.getName() + "^{tree}"));
            tw.addTree(repository.resolve(cur.getName() + "^^{tree}"));

            RenameDetector rd = new RenameDetector(repository);
            rd.addAll(DiffEntry.scan(tw));

            List<DiffEntry> diffs = rd.compute(tw.getObjectReader(), null);
            for(DiffEntry diff: diffs){
                if(diff.getScore() >= rd.getRenameScore() && diff.getOldPath().equals(file)){
                    formerName = diff.getNewPath();
                    break;
                }else if(diff.getOldPath().equals(file)){
                    formerName = diff.getNewPath();
                }else if(diff.getChangeType() == DiffEntry.ChangeType.ADD){
                    formerName = null;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return formerName;
        }
    }

    public ObjectId getId(String id){
        ObjectId result = null;
        try{
            result = repository.resolve(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public Patch getPatch(ObjectId newId, ObjectId oldId, String filePath){
        Patch patch = new Patch();
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser old = new CanonicalTreeParser();
            ObjectId oldTreeId = repository.resolve((oldId == null? newId.getName() + "^" : oldId.getName())+ "^{tree}");
            old.reset(reader, oldTreeId);

            CanonicalTreeParser n = new CanonicalTreeParser();
            ObjectId newTreeId = repository.resolve(newId.getName() + "^{tree}");
            n.reset(reader, newTreeId);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<DiffEntry> diffs = git.diff().setNewTree(n).setOldTree(old).
                    setPathFilter(PathFilter.create(filePath)).setOutputStream(out).call();
            String s = out.toString();
            patchString = s;

            byte[] bytes = s.getBytes();
            patch.parse(new ByteInputStream(bytes, bytes.length));
        } catch (Exception e) {
            System.out.println(" error + " + newId.getName() + "\n");
            System.out.println(e.getMessage());
        }
        return patch;
    }

    /**
     * 返回两个版本代码的差异
     * @param oldFile
     * @param newFile
     * @return 修改区域的list，但没有修改时，返回的内容大小为0，不会返回null。
     */
    public static EditList getEditList(String oldFile, String newFile) {
        RawText file1 = new RawText(oldFile.getBytes());
        RawText file2 = new RawText(newFile.getBytes());

        EditList diffList= new EditList();
        diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, file1, file2));
        return diffList;
    }

    public static Patch getPatch(String oldFile, String newFile){
        Patch patch = new Patch();
        EditList diffList= getEditList(oldFile, newFile);
        RawText file1 = new RawText(oldFile.getBytes());
        RawText file2 = new RawText(newFile.getBytes());

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new DiffFormatter(out).format(diffList, file1, file2);
            System.out.println(out.toString());
            byte[] bytes = out.toString().getBytes();
            patch.parse(new ByteInputStream(bytes, bytes.length));
        }catch (Exception e){
            e.printStackTrace();
        }
        return patch;
    }

    /**
     * 从commitMsg中获取issueID
     * @param commitMsg 某次commit中的message
     * @return commitMsg包含的issueId列表
     */
    public static List<String> findIssueId(String commitMsg){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(SOLR-[0-9]+)|(LUCENE-[0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(commitMsg);
        while(matcher.find()){
            result.add(matcher.group(0));
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
        GitAnalyzer a = new GitAnalyzer();
        System.out.println(a.getCommit("f92974be5295e55dcc64d71b120e30866018b539^").toString());
        String content = a.getFileFromCommit(a.getCommit("925197473b2cbfa3068041e6e12e537a5b66e2e6"), "bundles/org.eclipse.swt/Eclipse SWT Drag and Drop/motif/org/eclipse/swt/dnd/DragSource.java");
        String c2 = a.getFileFromFormerCommit(a.getCommit("f92974be5295e55dcc64d71b120e30866018b539"), "bundles/org.eclipse.swt/Eclipse SWT Drag and Drop/motif/org/eclipse/swt/dnd/DragSource.java");
        String c3 = a.getFileFromCommit(a.getCommit("f92974be5295e55dcc64d71b120e30866018b539^"), "bundles/org.eclipse.swt/Eclipse SWT Drag and Drop/motif/org/eclipse/swt/dnd/DragSource.java");
        int aa = 2;

    }

}