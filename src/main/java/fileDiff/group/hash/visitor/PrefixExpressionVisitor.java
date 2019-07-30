package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.PrefixExpression;

/**
 * Created by kvirus on 2019/7/25 0:13
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class PrefixExpressionVisitor extends ASTVisitor {
    public String variable = "";
    public String operator = "";

    @Override
    public boolean visit(PrefixExpression node) {
        variable = node.getOperand().toString();
        operator = node.getOperator().toString();
        return false;
    }
}
