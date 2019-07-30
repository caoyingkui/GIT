package util;

import javassist.compiler.Javac;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.Map;

/**
 * Created by kvirus on 2019/6/30 15:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class CompileTool {
    private static ASTParser parser = ASTParser.newParser(9);

    static {
        Map option = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
        parser.setCompilerOptions(option);
    }

    public static CompilationUnit getCommilationUnit(String code) {
        Map option = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
        parser.setCompilerOptions(option);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(code.toCharArray());

        return (CompilationUnit)parser.createAST(null);
    }

    public static Block getBlock(String code) {
        try {
            Map option = JavaCore.getOptions();
            JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
            parser.setCompilerOptions(option);
            parser.setKind(ASTParser.K_STATEMENTS);
            parser.setSource(code.toCharArray());

            Block block = (Block) parser.createAST(null);
            if (block.toString().equals("{\n}\n")) {
                return null;
            }

            return block;
        } catch (Exception e) {
            return null;
        }
    }

    public static Expression getExpression(String code) {
        try {
            Map option = JavaCore.getOptions();
            JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
            parser.setCompilerOptions(option);
            parser.setKind(ASTParser.K_EXPRESSION);
            parser.setSource(code.toCharArray());

            return (Expression) parser.createAST(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 当代码片段包裹在一个class内部时，用该方法获取方法内容。
     * 例如： class Test { public void fun() {...}}
     * @param code
     * @return
     */
    public static MethodDeclaration getMethodFromClass(String code) {
        Map option = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
        parser.setCompilerOptions(option);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        return declaration.getMethods()[0];
    }

    /**
     * 当代码片段就是一段方法代码的时候，用该方法获取方法内容。
     * 例如 public void fun() {...}
     * @param code
     * @return
     */
    public static MethodDeclaration getMethodFromMethod(String code) {
        try {
            Map option = JavaCore.getOptions();
            JavaCore.setComplianceOptions(JavaCore.VERSION_9, option);
            parser.setCompilerOptions(option);
            parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
            parser.setSource(code.toCharArray());
            return ((TypeDeclaration) parser.createAST(null)).getMethods()[0];
        } catch (Exception e) {
            System.out.println("error from compileTool");
            return null;
        }
    }

}
