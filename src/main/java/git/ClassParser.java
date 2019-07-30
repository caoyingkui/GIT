package git;


import fileDiff.type.FileType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;
import util.CompileTool;
import util.ReaderTool;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;

public class ClassParser {
    public FileType type;

    public String name = "";

    CompilationUnit unit = null;

    String sourceCode = "";

    List<Integer> lines = new ArrayList<>();

    String[] codeLines;

    int codeLength;

    List<Method> methods = new ArrayList<>();

    List<Field> fields = new ArrayList<>();

    List<Comment> comments = new ArrayList<>();

    public List<String> interfaces = new ArrayList<>();

    public String parent = "";


    public List<Method> getMethods() {
        return methods;
    }

    public List<Field> getFields() {
        return fields;
    }

    public ClassParser(){
        ;
    }

    public ClassParser(String filePath){
        String sourceCode = ReaderTool.read(filePath);
        initialize(sourceCode);
    }

    /**
     * 在解析出所有的field, method之后，需要将抽取的comment分配到相应的field或则函数上。
     */
    private void commentDispatch() {

        // region <创建comment到行数的映射>
        //建立comment和行数的关联
        //integer为一个comment出现的最后一行的行数
        //采取这样的方式是因为，比如给一个field，可以先推断这个field出现的行数n，
        //然后利用n-1，就可以知道是否存在comment
        Map<Integer, String> commentMap = new HashMap<>();
        for (int i = 0; i < comments.size(); i ++) {
            Comment comment = comments.get(i);
            if (comment.isBlockComment()) {
                BlockComment blockComment = (BlockComment)comment;
                int start = blockComment.getStartPosition(), end = start + blockComment.getLength();
                String content = blockCommentExtractor(sourceCode.substring(start, end));
                commentMap.put(getLine(end), content);
            } else if (comment.isDocComment()) {
                Javadoc javadocComment = (Javadoc) comment;
                int start = javadocComment.getStartPosition(), end = start + javadocComment.getLength();
                String content = javadocCommentExtractor(sourceCode.substring(start, end));

                //这里需要注意一下，javadoc类型的评论是会算作紧跟着后面的函数或域值的一部分。
                //例如
                // /** example **/
                // int example = 0;
                // 注释的起始位置是和example的起始位置是一样的
                // 因此呢，我们就把这个注释的最后一行，人为改变为起始位置的上一行，
                // 这样呢，逻辑上这个注释就在后面的类型的上一行。
                commentMap.put(getLine(start) - 1, content);
            } else if (comment.isLineComment()) {
                LineComment lineComment = (LineComment) comment;
                int start = lineComment.getStartPosition(), end = start + lineComment.getLength();
                String content = lineCommentExtractor(sourceCode.substring(start, end));
                StringBuilder builder = new StringBuilder(content);

                while (i + 1 < comments.size() && comments.get(i + 1).isLineComment()) {
                    LineComment nextComment = (LineComment) comments.get(i + 1);
                    int nextStart = getLine(nextComment.getStartPosition());
                    if (nextStart == start + 1) {
                        String nextContent = sourceCode.substring(nextStart, nextStart + nextComment.getLength());
                        builder.append(" ").append(lineCommentExtractor(nextContent));
                        i ++;
                    } else {
                        break;
                    }
                }
                commentMap.put(getLine(start), builder.toString());
            } else {
                assert(1 == 0); // 报错
            }
        }
        // endregion <创建comment到行数的映射>

        for (Field field: fields) {
            if (commentMap.containsKey(field.startLine - 1))
                field.comment = commentMap.get(field.startLine - 1);
        }

        for (Method method: methods) {
            if (commentMap.containsKey(method.startLine - 1))
                method.comment = commentMap.get(method.startLine - 1);
        }

    }

    /**
     * 去除comment中的特殊符号
     * @param comment
     * @return
     */
    public static String blockCommentExtractor(String comment) {
        // TODO 把该方法实现得完善一下
        return comment.substring(
                comment.indexOf("/*") + 2,
                comment.lastIndexOf("*/")
        ).trim();
    }

    public static String lineCommentExtractor(String comment) {
        return comment.substring(comment.indexOf("//") + 2).trim();
    }

    public static String javadocCommentExtractor(String comment) {
        return comment.substring(
                comment.indexOf("/**" ) + 3,
                comment.lastIndexOf("*/")
        ).trim();
    }

    /**
     * 获取类中定义的所有method名
     * @return
     */
    public Set<String> getAllMethodNames(){
        Set<String> result = new HashSet<>();
        Collections.sort(methods, (o1,o2 )->{
            if(o1.endLine < o2.startLine) return -1;
            else if(o2.endLine < o1.startLine) return 1;
            else return 0;
        });

        for(Method method: methods)
            result.add(method.fullName);
        return result;
    }

    /**
     * 获取position对应位置所在的行数
     * @param position 位置
     * @return 行数
     */
    public int getLine(int position){
        if(position < 0|| position >= codeLength)
            return -1;
        else{
            for(int i = 0 ; i < lines.size() - 1; i ++){
                if(position >= lines.get(i) && position < lines.get(i + 1))
                    return i;
            }
            return lines.size();
        }
    }

    /**
     * 给定一个位置，返回包含该位置的函数的下标
     * @param startPosition
     * @return
     */
    public int getMethodAt(int startPosition) {
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).startPos <= startPosition && methods.get(i).endPos >= startPosition)
                return i;
        }
        return -1;
    }

    public Map<String, Method> getChangedMethod(EditList editList, boolean isNew) {
        Map<String, Method> result = new HashMap<>();

        int start = -1, end = -1;
        if(isNew){
            for(Edit edit: editList){
                Edit.Type type = edit.getType();
                if(type == Edit.Type.INSERT ||
                        type == Edit.Type.REPLACE){
                    start = edit.getBeginB() ;
                    end = edit.getEndB() - 1;
                }else
                    continue;

                for(Method method: methods){
                    if(start >= method.startLine && start <= method.endLine ||
                            end >= method.startLine && end <= method.endLine ||
                            start <= method.startLine && end >= method.endLine) {
                        result.put(method.fullName, method);
                        //break; 不能加break;
                    }
                }

            }
        }else{
            for(Edit edit: editList){
                Edit.Type type = edit.getType();
                if(type == Edit.Type.DELETE ||
                        type == Edit.Type.REPLACE){
                    start = edit.getBeginA() ;
                    end = edit.getEndA() - 1;
                }else
                    continue;

                for(Method method: methods){
                    if(start >= method.startLine && start <= method.endLine ||
                            end >= method.startLine && end <= method.endLine ) {
                        result.put(method.fullName, method);
                        //break; 不能break
                    }
                }

            }
        }
        return result;
    }

    public Map<String, Method> getChangedMethod(Patch patch, boolean isNew){
        Map<String, Method> result = new HashMap<>();
        for(FileHeader file: patch.getFiles()){
            EditList list = null;
            try{
                list = file.toEditList();
            }catch (NullPointerException e){
                continue;
            }
            result.putAll(getChangedMethod(list, isNew));
        }
        return result;
    }

    /**
     * 初始化该对象，包括：
     *      每一行对应的起始-终止位置
     *      函数信息
     * @param sourceCode 源代碼
     */
    private void initialize(String sourceCode){

        unit = CompileTool.getCommilationUnit(sourceCode);
        getMetaInfo(unit);

        DeclarationVisitor visitor = new DeclarationVisitor(this);
        unit.accept(visitor);
        methods = visitor.methods;
        fields  = visitor.fields;

        comments = unit.getCommentList();
        commentDispatch();
    }

    private void getMetaInfo(CompilationUnit unit) {
        //获取接口信息
        TypeDeclaration type =  null;

        //特例：commit_ID: 2dfad693d6870e43a63aba3b43a62bf6c146b4c9
        //file: solr/core/src/java/org/apache/solr/util/hll/package-info.java
        if (unit.types().size() == 0) return;
        try {
            if (unit.types().get(0) instanceof TypeDeclaration ) {
                type = (TypeDeclaration) (unit.types().get(0));
                this.name = type.getName().toString();

                if (type.getSuperclassType() != null)
                    this.parent = type.getSuperclassType().toString();


                for (Object i : type.superInterfaceTypes()) {
                    if (i instanceof SimpleType) {
                        SimpleType t = (SimpleType) i;
                        interfaces.add(t.getName().toString());
                    }
                }
                //获取继承信息
                Object superType = type.getSuperclassType();
                if (superType instanceof SimpleType) {
                    SimpleType t = (SimpleType) superType;
                    parent = t.getName().toString();
                }

                if (type.isInterface()) this.type = FileType.Interface;
                else this.type = FileType.Class;
            } else if (unit.types().get(0) instanceof EnumDeclaration) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 初始化每一行对应的起始-终止位置
     */
    private void initializeLines(){
        lines = new ArrayList<>();
        int start = 0;
        for(int i = 0 ; i < codeLength ; i++){
            if(sourceCode.charAt(i) =='\n'){
                lines.add(start);
                start = i + 1;
            }
        }
    }

    /**
     * 设置新的sourceCode,并重新初始化
     * @param sourceCode
     */
    public ClassParser setSourceCode(String sourceCode) {

        this.sourceCode = sourceCode;
        this.codeLength = sourceCode.length();
        this.codeLines = sourceCode.split("\n");

        initializeLines();
        initialize(sourceCode);
        return this;
    }

    public boolean contains(String methodName) {
        for (Method method: methods) {
            if (method.fullName.equals(methodName))
                return true;
        }
        return false;
    }

    public static void main(String[] args){

        ClassParser parser = new ClassParser();
        String str = ReaderTool.read("file1.java");
        parser.setSourceCode(str);

    }
}
