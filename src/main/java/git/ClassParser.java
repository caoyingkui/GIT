package git;


import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import util.ReaderTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ClassParser {
    CompilationUnit unit;
    String sourceCode;
    List<Integer> lines;
    String[] codeLines;
    int codeLength;
    List<Method> methods;

    public List<Method> getMethods() {
        return methods;
    }

    public ClassParser(){
        ;
    }

    public ClassParser(String filePath){
        String sourceCode = ReaderTool.read(filePath);
        initialize(sourceCode);
    }

    /**
     * 从代码中抽取所有的method信息
     */
    private void extractAllMethods(){
        methods = new ArrayList<>();
        PackageDeclaration p = unit.getPackage();
        String packageName = "";
        if(p != null){
            packageName = p.getName().toString();
        }

        for(Object type: unit.types()){
            if(type instanceof TypeDeclaration){
                methods.addAll(extractAllMethods((TypeDeclaration)type, packageName));
            }
        }
    }

    /**
     * 从一个typeDeclaration中抽取所有的method信息
     * @param type
     * @param qualifiedName 递归抽取时,method的前缀名
     * @return
     */
    private List<Method> extractAllMethods(TypeDeclaration type, String qualifiedName) {
        List<Method> result = new ArrayList<>();
        String className = type.getName().toString();
        for(Object object: type.bodyDeclarations()){
            // region MethodDeclaration
            if(object instanceof MethodDeclaration){
                MethodDeclaration declaration = (MethodDeclaration) object;
                String methodName = declaration.getName().toString();
                String parStr = "";
                List parameters = declaration.parameters();
                if(parameters.size() > 0){
                    for(Object par: parameters){
                        if(par instanceof SingleVariableDeclaration){
                            parStr += ("," + ((SingleVariableDeclaration) par).getType().toString());
                        }
                    }
                    parStr = parStr.substring(1);
                }
                methodName += ("(" + parStr + ")");

                int startPosition = declaration.getStartPosition();
                int endPosition = startPosition + declaration.getLength() - 1;

                result.add(
                        new Method(qualifiedName + "." + className + "." + methodName,
                                getLine(startPosition),
                                getLine(endPosition)
                        )
                );
            }
            //endregion
            else if(object instanceof TypeDeclaration){
                TypeDeclaration subType = (TypeDeclaration) object;
                result.addAll(extractAllMethods(subType, qualifiedName + "." + type.getName().toString()));
            }

        }
        return result;
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
    int getLine(int position){
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

    public Set<String> getChangedMethod(Patch patch, boolean isNew){
        Set<String> result = new HashSet<>();
        for(FileHeader file: patch.getFiles()){

            EditList list = null;
            try{
                list = file.toEditList();
            }catch (NullPointerException e){
                continue;
            }

            int start = -1, end = -1;
            if(isNew){
                for(Edit edit: list){
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
                            result.add(method.fullName);
                            //break; 不能加break;
                        }
                    }

                }
            }else{
                for(Edit edit: list){
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
                            result.add(method.fullName);
                            //break; 不能break
                        }
                    }

                }
            }
        }
        return result;
    }

    public String getSubString(int startLine, int endLine) {
        String result = "";
        for (int line = startLine; line <= endLine; line ++) {
            result += codeLines[line] + "\n";
        }
        return result;
    }

    /**
     * 初始化该对象，包括：
     *      每一行对应的起始-终止位置
     *      函数信息
     * @param sourceCode
     */
    private void initialize(String sourceCode){
        this.sourceCode = sourceCode;
        this.codeLength = sourceCode.length();
        this.codeLines = sourceCode.split("\n");
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(sourceCode.toCharArray());
        unit = (CompilationUnit) parser.createAST(null);
        initializeLines();
        extractAllMethods();
    }

    /**
     * 初始化每一行对应的起始-终止位置
     */
    private void initializeLines(){
        lines = new ArrayList<>();
        int length = sourceCode.length();
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
        initialize(sourceCode);
        return this;
    }

    public static void main(String[] args){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("code.txt")));
            String line;
            String code = "";
            while ((line = reader.readLine()) != null) {
                code += line + "\n";
            }
            reader.close();

            ClassParser parser = new ClassParser(code);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
