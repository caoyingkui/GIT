package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/7/13 23:29
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ForeachVisitor extends ASTVisitor {
    public String type = "";

    public String expression = "";

    @Override
    public boolean visit(EnhancedForStatement node) {
        SingleVariableDeclaration de = node.getParameter();
        Expression exp = node.getExpression();

        type = de.getType().toString();
        expression = exp.toString();

        de.accept(new ASTVisitor() {
            @Override
            public boolean visit(ParameterizedType node) {
                ASTVisitor visitor = new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        node.setIdentifier("V");
                        return false;
                    }
                };

                node.typeArguments().forEach(argument -> {
                    ((ASTNode)argument).accept(visitor);
                });

                type = node.toString(); // 如果带泛型的类型，则用修改后的类型替换
                return false;
            }
        });

        exp.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                expression = node.getName().toString(); // 如果是方法调用的话，就用方法名替换
                return false;
            }
        });
        return false;
    }
}
