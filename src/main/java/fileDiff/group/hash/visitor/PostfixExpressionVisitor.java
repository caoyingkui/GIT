package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.PostfixExpression;

/**
 * Created by kvirus on 2019/6/20 20:36
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class PostfixExpressionVisitor extends ASTVisitor {
    public String op = "";
    public String expression = "";
    @Override
    public boolean visit(PostfixExpression node) {
        op = node.getOperator().toString();
        expression = node.getOperand().toString();
        return false;
    }
}
